# Standardized Sample App Spec - README

## Top level Requirements

### Where the Sample Apps should live

Each sample app that follows the requirements below should be stored in the `sample-apps` folder in https://github.com/aws-observability/aws-otel-community.
The README file in the `sample-apps` directory contains a matrix which lists the sample apps in conformance to this document. New sample apps must be added to this matrix. The matrix must display the status of Metrics Logs and Traces (Stable or Unstable).

### How the Sample Apps should work

Each sample app must emit the same Metrics, Logs and Traces according to this specification. The sample app README should list any missing signal type in it's implementation. 
Each Sample app should be able to be built as an image. The sample app must be able to accept configuration through a configuration file (YAML) and environment variables.

### Assumption on how the Sample Apps should emit Traces and Metrics

#### Traces

Trace generation in the sample app should reflect a real life cycle or flow.
In existing sample applications, Trace instrumentation is coupled with a **Traffic Generator** (https://github.com/aws-observability/aws-otel-test-framework/tree/terraform/traffic-generator) that makes http requests to the sample app and emits traces based on those requests. In another example, **ho11y** (https://gitlab.aws.dev/hausenbl/); sample apps can create their own traffic by invoking other sample apps (invocation pattern). 


Sample apps in conformance to this document will also include the invocation pattern as an optional configuration. This will allow for more extensive testing and demos if configured.
An example of the invocation pattern is shown below.

![Alt text](./invocation.png?raw=true "Invocation")

#### Metrics

The two patterns of metrics that will be generated are **Request-based** and **Random**. Request based metrics will also assume an invocation has been made to the Sample App. Random can be arbitrarily generated for metrics that do not correlate to incoming requests. 

## Documentation Requirements

Each sample app MUST be coupled with a README document that will go into each sample app’s code base detailing how to setup the sample app (this should be same across all sample apps); although redundant, each repository should still have it to avoid looking across sample apps for a README. This README should also contain the following sections:

* **Description** - Information about the language being used and a link to this Spec document for context
* **Non-conformance** - Documentation on missing features or missing extensions that don’t allow a given sample app to conform with this document
* **Workarounds** - Examples of workarounds include the use of an unstable API or SDK to produce signals. This `workaround` code may require changes in the future.


## Functional Requirements

Each sample app should be configured with manual instrumentation from OTel. Auto instrumentation can also be implemented.
Metrics, traces, and logs will all use an OTLP exporter to ship data from the sample app.
The model of how each sample app should function is displayed below:

![Alt text](./sampleapp.png?raw=true "Sample App")

### Configuration

**Metric type color code:** Counter (Green), Histogram (Orange), UpDownCounter (Blue), Gauge (Purple)
The configuration file configures Host, Port, TimeAlive, TotalHeapSize, ThreadsActive, and CpuUsage. It will also configure TimeInterval. The following example YAML file demonstrates the structure and arbitrarily chosen default values for the previously mentioned configurable metrics.
This configuration file should be set by an environment variable (SAMPLE_APP_CONF) or else it will default to the below snippet. If two sample apps are deployed by default, there will be an error for using the same port twice.

```
---
Host: "0.0.0.0"                       # Host - String Address
Port: "4567"                          # Port - String Port
TimeInterval: 1                       # Interval - Time in seconds to generate new metrics
RandomTimeAliveIncrementer: 1         # Metric - Amount to incremement metric by every TimeInterval
RandomTotalHeapSizeUpperBound: 100    # Metric - UpperBound for TotalHeapSize for random metric value every TimeInterval
RandomThreadsActiveUpperBound: 10     # Metric - UpperBound for ThreadsActive for random metric value every TimeInterval
RandomCpuUsageUpperBound: 100         # Metric - UppperBound for CpuUsage for random metric value every TimeInterval
SampleAppPorts: []                    # Sampleapp ports to make calls to   
ResourceDetector: ''                  # String to specify resource detector
```

Every sample app will assume a configuration file with these variable names.
The testing framework will validate the existence of these Metrics in the sample apps for certain languages with a stable upstream SDK & API.   

### Metrics

The sample app must include the 7 metrics listed below. The sample app must implement the name, unit and description as defined in the spec. The request based metrics will also be generated alongside traces from incoming requests from invocations or incoming traffic. Any languages that are not stable in metrics or are unable to be implemented under this structure must include documentation on that issue in their respective **Documentation** document as mentioned in the **Documentation requirements**.

**Request based:**

AsyncCounter

* Name: totalApiRequests
* Unit: “1”
* Description: “Increments by one every time a sampleapp endpoint is used”

Counter

* Name: totalBytesSent
* Unit: “By”
* Description: “Keeps a sum of the total amount of bytes sent while the application is alive”
* Includes Callback Function

Histogram

* Name: latencyTime
* Unit: “ms”
* Description: “Measures latency time in buckets of 100 300 and 500”

**Common Attributes for Request based metrics**

These are Key Value pairs to be added on metrics.
```
{
“signal”:     (string)“metric”
“language”:   (string)<name of language used. Should be set to the name of the sample app preceeding "-sample-app" for standardization purposes>
“metricType”: (string)“request”
}
```

**Random:**

Counter

* Name: timeAlive
* Unit: “ms”
* Description: “Total amount of time that the application has been alive”

Asynchronous UpDown Counter

* Name: totalHeapSize
* Unit: “By”
* Description: “The current total heap size”
* Includes Callback Function

UpDown Counter

* Name: threadsActive
* Unit: “1”
* Description: “The total number of threads active”

Asynchronous Gauge

* Name: cpuUsage
* Unit: “1”
* Description: “Cpu usage percent”
* Includes Callback Function

**Common Attributes for Random based metrics**

These are Key Value pairs to be added on metrics and will be tested for.
```
{
“signal”:     (string)“metric”
“language”:   (string)<name of language used. Should be set to the name of the sample app preceeding "-sample-app" for standardization purposes> 
“metricType”: (string)“random”
}
```

Upon implementation, the Metric names should also have the instance ID appended onto the end of the name like so "\<metric name\>_\<instance ID\>".  The instance ID can be retrieved through the INSTANCE_ID environment variable.  An example of what this would look like: `cpuUsage_a1b2c3d4e5f6g7h8`.

### Logs

**Logs are OPTIONAL due to their status of preview upstream. Logs must not be tested against nor will the logging structure described be final. It is to be used as an example to construct and emit example logs.**
Logs will be displayed in JSON format for simplicity and readability.  Logs will be sent out on three different occasions, requests, errors, and events. An example of each can be seen underneath this section of the document. We will try to keep logs consistent in all sample applications according to the Logs Data Model (https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/logs/data-model.md) found in the opentelemetry-specification repository.

Logs will contain the following fields:

* Timestamp
* TraceID
* SpanID
* SeverityText (or LogLevel)
* Body

```
Info Logs 

// Info Logs are usually sent on application startup.

{
"level":"info",
"msg":"Launching sampleapp: I am [sampleapp-svc] listening on port :8080 on all local IPs.",
"time":"2022-06-28T11:31:38-07:00"
}
```

```
Event Log

// Upon any event logs are sent out.

{
"event":"invoke","level":"info","msg":"sampleapp was invoked",
"remote":"127.0.0.1:63875","time":"2022-06-27T15:58:35-07:00",
"traceID":"1-62ba361b-0dc302c06b6bc21c2b2152a4"
}
```

### Traces

At minimum, four GET requests endpoints must be present in each sample app. 

**1. GET - /**

The first endpoint will serve as a health check to confirm that the app has successfully ran.

**2. GET - /outgoing-http-call**

The second endpoint will make an HTTP GET request to aws.amazon.com (http://aws.amazon.com/). It will generate Traces and if applicable, Metrics.

**3. GET - /aws-sdk-call**

The third endpoint will make a request to AWS S3 and if credentials are provided, it will list all S3 buckets in the provided users S3.

**4. GET - /outgoing-sampleapp**

The fourth endpoint will make GET request to all other sample app endpoints that are configured. If no other sample apps are configured (leaf case), it will make a request to www.amazon.com (http://www.amazon.com/). 


An AWS X-Ray Trace ID should also be returned at the end of each request.
Each sample app MUST register an AWS X-Ray Propagator.
Each sample app should initialize a Tracer with the label ADOT-Tracer-Sample.  
Spans that are added to a trace should be labeled as \<procedure-name\> where the procedure is a traceable event.
The events that must be present in every sample app are the following in respective order to the 4 endpoints.

1. n/a
2. “outgoing-http-call”
3. “aws-sdk-call”
4. “invoke-sample-apps” 
    1. “invoke-sampleapp”
    2. “leaf-request”

These span names will be tested.

The fourth endpoint must create a span that will have potentially two child spans. “invoke-sampleapp” is the case where there are more than 0 sample apps configured to make a call to.
“leaf” request is the case where there are no sample apps to make a call to.

**Common Attributes for Trace spans**

These are Key Value pairs to be added on traces and will be tested for as well.
```
{
“signal”:     (string)“trace”
“language”:   (string)<name of language used. Should be set to the name of the sample app preceeding "-sample-app" for standardization purposes>
}
```

### Interactions

Each sample app should interact with at least one AWS service.
An example of this would be interacting with S3 by tracing a request to get all buckets owned by an authenticated user.
This example requires s3:ListAllMyBuckets permissions. Each sample app should also provide a description on how to set up this interaction if desired. If not desired then the sample app should just fail the request and emit the corresponding trace.

### AWS Resource detectors

If possible (e.g. resource detectors are available in the given language), each sample app should include resource detectors (ECS, EKS, EC2)
The configuration file has a field ResourceDetector which will be used to specify which resource detector to use.
