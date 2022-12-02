# Sample Apps


List of sample apps across all repositories in [aws-observability](https://github.com/aws-observability) org.

|Sample App                   |Location                                                                                                                                    |App functionality                                                                                                                              |Language  |
|-----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|----------|
|Prometheus-sample-app        |[aws-otel-community](https://github.com/aws-observability/aws-otel-community/tree/master/sample-apps/prometheus-sample-app)                 |Generates prometheus's metrics (counter, gauge, histogram,summary)                                                                             |Go        |  
|Javascript sample app        |[aws-otel-js](https://github.com/aws-observability/aws-otel-js/tree/main/sample-apps)                                                       |Continuous integration of ADOT components for X-Ray with Manual instrumentation of OpenTelemetry JavaScript SDK                                |JavaScript|
|Go Sample app                |[aws-otel-go](https://github.com/aws-observability/aws-otel-python/tree/main/integration-test-apps/none-instrumentation/flask)              |Complement the upstream OpenTelemetry Go with components for X-Ray                                                                             |Go        |
|.Net Sample app              |[aws-otel-dotnet](https://github.com/aws-observability/aws-otel-dotnet/tree/main/integration-test-app)                                      |Validates the continual integration with the AWS Distro for OpenTelemetry .NET and AWS X-Ray back-end service                                  |.Net      |
|Jmx                          |[aws-otel-test-framework](https://github.com/aws-observability/aws-otel-test-framework/tree/terraform/sample-apps/jmx)                      |Generates prometheus metrics                                                                                                                   |Java      |
|Jaeger-Zipkin                |[aws-otel-test-framework](https://github.com/aws-observability/aws-otel-test-framework/tree/terraform/sample-apps/jaeger-zipkin-sample-app) |Emits trace data using zipkin and jaeger                                                                                                       |Java      |
|Statsd                       |[aws-otel-testframework](https://github.com/aws-observability/aws-otel-test-framework/tree/terraform/sample-apps/statsd)                    |Emits metrics in statsd format                                                                                                                 |Python    |
|Prometheus sample app        |[aws-otel-test-framework](https://github.com/aws-observability/aws-otel-test-framework/tree/terraform/sample-apps/prometheus)               |Generates prometheus's metrics (counter, gauge, histogram,summary)                                                                             |Go        |


**Python instrumentation sample apps**

|Sample App                   |Location                                                                                                                                    |App functionality                                                                                                                              |
|-----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
|Python-auto instrumentation  |[aws-otel-python](https://github.com/aws-observability/aws-otel-python/tree/main/integration-test-apps/auto-instrumentation/flask)          |Continuous integration of ADOT components for X-Ray with Auto instrumentation of OpenTelemetry Python                                          |
|Python-manual instrumentation|[aws-otel-python](https://github.com/aws-observability/aws-otel-python/tree/main/integration-test-apps/manual-instrumentation/flask)        |Continuous integration of ADOT components for X-Ray with manual instrumentation of OpenTelemetry Python                                        |
|Python-none instrumentation  |[aws-otel-python](https://github.com/aws-observability/aws-otel-python/tree/main/integration-test-apps/none-instrumentation/flask)          |This application provides a baseline for performance testing, has no instrumentation, helps reveal the overhead that comes with instrumentation|


**Java instrumentation sample apps**

|Sample App                   |Location                                                                                                                                    |App functionality                                                                                                                              |
|-----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
|Spark-awssdk1                |[aws-otel-java-instrumentation](https://github.com/aws-observability/aws-otel-java-instrumentation/tree/main/sample-apps/spark-awssdkv1)    |Generates OTLP metrics and traces                                                                                                              |
|Spark                        |[aws-otel-java-instrumentation](https://github.com/aws-observability/aws-otel-java-instrumentation/tree/main/sample-apps/spark)             |Generates OTLP metrics and traces                                                                                                              |
|Springboot                   |[aws-otel-java-instrumentation](https://github.com/aws-observability/aws-otel-java-instrumentation/tree/main/sample-apps/springboot)        |Generates OTLP metrics and traces                                                                                                              |


**Ruby instrumentation sample app**

|Sample App                   |Location                                                                                                                                    |App functionality                                                                                                                              |
|-----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
|Ruby-manual-instrumentation  |[aws-otel-ruby](https://github.com/aws-observability/aws-otel-ruby/tree/main/sample-apps/manual-instrumentation/ruby-on-rails)              |Cotinuous integration of ADOT X-Ray components and X-Ray service. Manual Instrumentation using OpenTelemetry Ruby                              |

## Standardized Sample App Spec - README

### Top level Requirements

#### Where the Sample Apps should live

Each sample app that follows the requirements below should exist in a single repository as well as with this document. They will exist under the sample apps folder in https://github.com/aws-observability/aws-otel-community.
The matrix that exists in the README should be updated with the sample apps in conformance to this document. The matrix must additionally display the status of Metrics Logs and Traces. (Stable or Unstable)

#### How the Sample Apps should work

Each sample app should emit the same Metrics, Logs and Traces. If a sample app cannot emit the mentioned, then that will noted in that sample app’s documentation.
Each Sample app should be able to be exist as a docker image and must be able to be configured through a configuration file (YAML) and environment variables. The images will be stored in Public ECR. This means that each Sample app should support values being provided by a configuration file or have default values if none is provided. For the purpose of our use cases, the sample apps should be configured through a configuration file and then deployed as an image. The purpose of configuration support is there to provide flexibility in case future changes occur that require different telemetry to be emitted. The default values and what type of telemetry for our specific use cases will remain constant and will be detailed later in this document. 

#### Assumption on how the Sample Apps should emit Traces and Metrics

**Traces** are not something that could or should be generated arbitrarily but instead should reflect a real life cycle or flow.
In existing sample applications, Trace instrumentation is coupled with a **Traffic Generator** (https://github.com/aws-observability/aws-otel-test-framework/tree/terraform/traffic-generator) that makes http requests to the Sample app and emits traces based on that those requests. In another example, **ho11y** (https://gitlab.aws.dev/hausenbl/); sample apps can create their own traffic by invoking other sample apps (invocation pattern). 

**Pros of adding invocation pattern**

* Better demonstration of context propagation
* Sample apps across different languages can call each other which better represents a distributed system
* Is an optional configuration 

**Cons of adding invocation pattern**

* Configuring each sample app to call each other is not as straight forward as just having a traffic generator
* Another layer of configurability is added

Sample apps in conformance to this document will also include the invocation pattern as an optional configuration. This will allow for more extensive testing and demos if configured.
A possible example can be seen below. This invocation capability can allow testing to be as complex or simple as needed.

![Alt text](./invocation.png?raw=true "Invocation")

**Metrics** can be more loosely generated if they are not related to incoming requests. The two patterns of metrics that will be generated can be categorized as **Request-based** and **Random**. Request based metrics will also assume an invocation. Random can be arbitrarily generated for metrics that do not correlate to incoming requests. 

### Documentation Requirements

Each sample app MUST be coupled with a document that will have the following sections:

* **Description** - Information about the language being used and a link to this document for context
* **Non-conformance** - Documentation on missing features or missing extensions that don’t allow a given sample app to conform with this document
* **Workarounds** - If a given sample app is using any workarounds to conform to this document (such as using unstable sdk & api OR writing in the variable names) then the sections of the code that could break in the future must be documented
* **README** - The readme that will go into each sample app’s code base detailing how to setup the sample app (this should be same across all sample apps); although redundant, each repository should still have it to avoid looking across sample apps for a readme. This readme should also include the Non-conformance section specific to the sample app it belongs to.

### Functional Requirements

In a perfect world where everything is stable upstream, every sample app should emit the same Metrics, Logs, and Traces if given a specific routine (e.g. Python and Go sample apps both receive the same 3 requests → both emit the same signals in the same format). The following specifications will detail what exactly will be tracked as a metric log or trace. Each sample app should be configured with manual instrumentation from OTEL. Auto instrumentation can also be implemented.
Metrics and traces should be OTLP and Logs should be in JSON format using any popular logging library for each respective language with the appropriate license. (e.g. Log4j for java, Logrus for Go)
The model of how each sample app should function is displayed below:

![Alt text](./sampleapp.png?raw=true "Sample App")

**Metric type color code:** Counter (Green), Histogram (Orange), UpDownCounter (Blue), Gauge (Purple)
The configuration file configures Host, Port, TimeAlive, TotalHeapSize, ThreadsActive, and CpuUsage. It will also configure TimeInterval. The following example YAML file demonstrates the structure and arbitrarily chosen default values for the previously mentioned configurable metrics.
This configuration file should be set by an environment variable (SAMPLE_APP_CONF) or else it will default to the below snippet. If two sample apps are deployed by default, there will be an error for using the same port twice.

```
---
Host: "0.0.0.0"                       # Host - String Address
Port: "8080"                          # Port - String Port
TimeInterval: 1                       # Interval - Time in seconds to generate new metrics
RandomTimeAliveIncrementer: 1         # Metric - Amount to incremement metric by every TimeInterval
RandomTotalHeapSizeUpperBound: 100    # Metric - UpperBound for TotalHeapSize for random metric value every TimeInterval
RandomThreadsActiveUpperBound: 10     # Metric - UpperBound for ThreadsActive for random metric value every TimeInterval
RandomCpuUsageUpperBound: 100         # Metric - UppperBound for CpuUsage for random metric value every TimeInterval
SampleAppPorts: []                    # Sampleapp ports to make calls to   
ResourceDetector: ''                  # String to specify resource detector
```

Every sample app will assume a configuration file with these variable names.
The testing framework MUST not test against these Metrics due to upstream unstable SDK & API in certain languages. These are mainly for being able to demonstrate some type of metrics being generated in languages that support it. 

#### Metrics

For Metrics, each sample app must include the 6 different types of instruments to help showcase how each instrument can be utilized in varying contexts when gathering metrics. Each sample app must implement every instrument in our included languages and keep variable names, units, descriptions, and outputs consistent across all of them. The request based metrics will also be generated alongside traces from incoming requests from invocations or incoming traffic. Any languages that are not stable in metrics or are unable to be implemented under this structure must include documentation on that issue in their respective **Documentation** document as mentioned in the **Documentation requirements**.

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
“language”:   (string)<name of language used>
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
These are Key Value pairs to be added on metrics.
```
{
“signal”:     (string)“metric”
“language”:   (string)<name of language used>
“metricType”: (string)“random”
}
```

#### Logs

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

#### Traces

At minimum, four GET requests endpoints must be present in each sample app. 
The first endpoint will serve as a health check to confirm that the app has successfully ran.
The second endpoint will make an HTTP GET request to aws.amazon.com (http://aws.amazon.com/). It will generate Traces and if applicable, Metrics.
The third endpoint will make a request to AWS S3 and if credentials are provided, it will list all S3 buckets in the provided users S3.
The fourth endpoint will make GET request to all other sample app endpoints that are configured. If no other sample apps are configured (leaf case), it will make a request to www.amazon.com (http://www.amazon.com/). 

1. GET - /
2. GET - /outgoing-http-call
3. GET - /aws-sdk-call
    
4. GET - /outgoing-sampleapp

An AWS X-Ray Trace ID should also be returned at the end of each request.
Each sample app MUST register an AWS X-Ray Propagator.
Each sample app should initialize a Tracer with the label ADOT-Tracer-Sample.  
Spans that are added to a trace should be labeled as \<procedure-name\> where the procedure is a traceable event.
The events that must be present in every sample app are the following in respective order to the 4 endpoints.

1. n/a
2. “outgoing-http-call”
3. “get-aws-s3-buckets”
4. “invoke-sample-apps” 
    1. “invoke-sampleapp”
    2. “leaf-request”

The fourth endpoint must create a span that will have potentially two child spans. “invoke-sampleapp” is the case where there are more than 0 sample apps configured to make a call to.
“leaf” request is the case where there are no sample apps to make a call to.
**Common Attributes for Trace spans**
These are Key Value pairs to be added on metrics.
```
{
“signal”:     (string)“trace”
“language”:   (string)<name of language used>
}
```

#### Interactions

Each sample app should interact with at least one AWS service.
An example of this would be interacting with S3 by tracing a request to get all buckets owned by an authenticated user.
This example requires s3:ListAllMyBuckets permissions. Each sample app should also provide a description on how to set up this interaction if desired. If not desired then the sample app should just fail the request and emit the corresponding trace.

#### AWS Resource detectors

If possible (e.g. resource detectors are available in the given language), each sample app should include resource detectors (ECS, EKS, EC2)
The configuration file has a field ResourceDetector which will be used to specify which resource detector to use.




