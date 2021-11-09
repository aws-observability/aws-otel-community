# OpenTelemetry Collector Processor Exploration

## Objective

To describe a user experience and strategies for configuring processors in the OpenTelemetry collector.

## Summary

The OpenTelemetry (OTel) collector is a tool to set up pipelines to receive telemetry from an application and export it
to an observability backend. Part of the pipeline can include processing stages, which executes various business logic
on incoming telemetry before it is exported.

Over time, the collector has added various processors to satisfy different use cases, generally in an ad-hoc way to
support each feature independently. We can improve the experience for users of the collector by consolidating processing
patterns in terms of user experience, and this can be supported by defining a querying model for processors
within the collector core, and likely also for use in SDKs, to simplify implementation and promote the consistent user
experience and best practices.

## Goals and non-goals

Goals:
- List out use cases for processing within the collector
- Consider what could be an ideal configuration experience for users

Non-Goals:
- Merge every processor into one. Many use cases overlap and generalize, but not all of them
- Technical design or implementation of configuration experience. Currently focused on user experience.

## Use cases for processing

### Telemetry mutation

Processors can be used to mutate the telemetry in the collector pipeline. OpenTelemetry SDKs collect detailed telemetry
from applications, and it is common to have to mutate this into a way that is appropriate for an individual use case.

Some types of mutation include

- Remove a forbidden attribute such as `http.request.header.authorization`
- Reduce cardinality of an attribute such as translating `http.target` value of `/user/123451/profile` to `/user/{userId}/profile`
- Decrease the size of the telemetry payload by removing large resource attributes such as `process.command_line`
- Filtering out signals such as by removing all telemetry with a `http.target` of `/health`
- Attach information from resource into telemetry, for example adding certain resource fields as metric dimensions

The processors implementing this use case are `attributesprocessor`, `filterprocessor`, `metricstransformprocessor`, 
`resourceprocessor`, `spanprocessor`.

### Metric generation

The collector may generate new metrics based on incoming telemetry. This can be for covering gaps in SDK coverage of
metrics vs spans, or to create new metrics based on existing ones to model the data better for backend-specific
expectations.

- Create new metrics based on information in spans, for example to create a duration metric that is not implemented in the SDK yet
- Apply arithmetic between multiple incoming metrics to produce an output one, for example divide an `amount` and a `capacity` to create a `utilization` metric

The processors implementing this use case are `metricsgenerationprocessor`, `spanmetricsprocessor`.

### Grouping

Some processors are stateful, grouping telemetry over a window of time based on either a trace ID or an attribute value,
or just general batching.

- Batch incoming telemetry before sending to exporters to reduce export requests
- Group spans by trace ID to allow doing tail sampling
- Group telemetry for the same path

The processors implementing this use case are `batchprocessor`, `groupbyattrprocessor`, `groupbytraceprocessor`.

### Telemetry enrichment

OpenTelemetry SDKs focus on collecting application specific data. They also may include resource detectors to populate
environment specific data but the collector is commonly used to fill gaps in coverage of environment specific data.

- Add environment about a cloud provider to `Resource` of all incoming telemetry

The processors implementing this use case are `k8sattributesprocessor`, `resourcedetectionprocessor`.

## Telemetry query language

When looking at the use cases, there are certain common features for telemetry mutation and metric generation.

- Identify the type of signal (span, metric, log, resource), or apply to all signals
- Navigate to a path within the telemetry to operate on it
- Define an operation, and possibly operation arguments

We can try to model these into a query language, in particular allowing the first two points to be shared among all
processing operations, and only have implementation of individual types of processing need to implement operators that
the user can use within an expression.

Telemetry is modeled in the collector as [`pdata`](https://github.com/open-telemetry/opentelemetry-collector/tree/main/model/pdata)
which is roughly a 1:1 mapping of the [OTLP protocol](https://github.com/open-telemetry/opentelemetry-proto/tree/main/opentelemetry/proto).
This data can be navigated using field expressions, which are fields within the protocol separated by dots. For example,
the status message of a span is `status.message`. A map lookup can include the key as a string, for example `attributes["http.status_code"]`.

Virtual fields can be defined for the `type` of a signal (`span`, `metric`, `log`, `resource`) and the resource for a
telemetry signal. For metrics, the structure presented for processing is actual data points, e.g. `NumberDataPoint`, 
`HistogramDataPoint`, with the information from higher levels like `Metric` or the data type available as virtual fields.

Navigation can then be used with a simple expression language for identifying telemetry to operate on.

`... where name = "GET /cats"`
`... where type = span and attributes["http.target"] = "/health"`
`... where resource.attributes["deployment"] = "canary"`
`... where type = metric and descriptor.metric_type = gauge`
`... where type = metric and descriptor.metric_name = "http.active_requests"`

Having selected telemetry to operate on, any needed operations can be defined as functions. Known useful functions should
be implemented within the collector itself, provide registration from extension modules to allow customization with
contrib components, and in the future can even allow user plugins possibly through WASM, similar to work in 
[HTTP proxies](https://github.com/proxy-wasm/spec). The arguments to operations will primarily be field expressions,
allowing the operation to mutate telemetry as needed.

### Examples

Remove a forbidden attribute such as `http.request.header.authorization` from all telemetry.

`delete(attributes["http.request.header.authorization"])`

Remove a forbidden attribute from spans only

`delete(attributes["http.request.header.authorization"]) where type = span`

Remove all attributes except for some

`keep(attributes, "http.method", "http.status_code") where type = metric`

Reduce cardinality of an attribute

`replace_wildcards("/user/*/list/*", "/user/{userId}/list/{listId}", attributes["http.target"])`

Reduce cardinality of a span name

`replace_wildcards("GET /user/*/list/*", "GET /user/{userId}/list/{listId}", name) where type = span`

Decrease the size of the telemetry payload by removing large resource attributes

`delete(attributes["process.command_line"]) where type = resource)`

Filtering out signals such as by removing all telemetry with a `http.target` of `/health`

`drop() where attributes["http.target"] = "/health"`

Attach information from resource into telemetry, for example adding certain resource fields as metric attributes

`set(attributes["k8s_pod"], resource.attributes["k8s.pod.name"]) where type = metric`

Stateful processing can also be modeled by the language. The processor implementation would set up the state while
parsing the configuration.

Create duration_metric with two attributes copied from a span

```
create_histogram("duration", end_time_nanos - start_time_nanos) where type = span
keep(attributes, "http.method") where type = metric and descriptor.metric_name = "duration
```

Group spans by trace ID

`group_by(trace_id, 2m) where type = span`

Create utilization metric from base metrics. Because navigation expressions only operate on a single piece of telemetry,
helper functions for reading values from other metrics need to be provided.

`create_gauge("pod.cpu.utilized", read_gauge("pod.cpu.usage") / read_gauge("node.cpu.limit") where type = metric`

A lot of processing. Queries are executed in order. While initially performance may degrade compared to more specialized
processors, the expectation is that over time, the query processor's engine would improve to be able to apply optimizations 
across queries, compile into machine code, etc.

```yaml
receivers:
  otlp:

exporters:
  otlp:

processors:
  query:
    # Assuming group_by is defined in a contrib extension module, not baked into the "query" processor
    extensions: [group_by]
    expressions:
      - drop() where attributes["http.target"] = "/health"
      - delete(attributes["http.request.header.authorization"])
      - replace_wildcards("/user/*/list/*", "/user/{userId}/list/{listId}", attributes["http.target"])
      - set(attributes["k8s_pod"], resource.attributes["k8s.pod.name"]) where type = metric
      - group_by(trace_id, 2m) where type = span

pipelines:
  - receivers: [otlp]
    exporters: [otlp]
    processors: [query]
```

## Declarative configuration

The telemetry query language presents an SQL-like experience for defining telemetry transformations - it is made up of
the three primary components described above, however, and can be presented declaratively instead depending on what makes
sense as a user experience.

```yaml
- type: span
  filter:
    match:
      path: status.code
      value: OK
  operation:
    name: drop
- type: all
  operation:
    name: delete
    args:
      - attributes["http.request.header.authorization"]
```

An implementation of the query language would likely parse expressions into this sort of structure so given an SQL-like
implementation, it would likely be little overhead to support a YAML approach in addition.
