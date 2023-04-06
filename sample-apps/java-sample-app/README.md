### Application interface

This Java sample app will emit Traces and Metrics. There are two types of metrics emitted;
Request Based and Random Based.
Metrics are generated as soon as the application is ran or deployed without any additional effort. These are considered the random based metrics which track a mock of TimeAlive, TotalHeapSize, ThreadsActive and CpuUsage. The boundaries for these metrics are standard and can be found in the configuration file (YAML) called config.yaml.

Additionally, you can generate Traces and request based Metrics by making requests to the following exposed endpoints:

1. /
    1. Ensures the application is running
2. /outgoing-http-call
    1. Makes a HTTP request to aws.amazon.com (http://aws.amazon.com/)
3. /aws-sdk-call
    1. Makes a call to AWS S3 to list buckets for the account corresponding to the provided AWS credentials
4. /outgoing-sampleapp
    1. Makes a call to all other sample app ports configured at `<host>:<port>/outgoing-sampleapp`. If none available, makes a HTTP request to www.amazon.com (http://www.amazon.com/)

There are two type of Java sample application that expose the exact same metrics and endpoints:

* Auto - No code is necessary to instrument supported third party libraries and the initialization of opentelemetry is done through system properties.
* Manual - All setup needs to be done explicitly using Java code.

[Sample App Spec](../SampleAppSpec.md)

* Non-conformance: This SDK language is not missing any features or extensions required other than Resource Detectors
* Workarounds: No workarounds are being used in this application

### Requirements

* JDK 1.8+
* Gradle 7.1.1

### Running the application

In order to run the application, please follow the steps:

- Clone the repository
  `git clone https://github.com/aws-observability/aws-otel-community.git`
- Switch into the directory
  `cd sample-apps/java-sample-app`
- Run the sample application
  * `./gradlew <type>:run`
  * `<type>` can either `manual` or `auto`. For example. to run the application using the auto instrumentation you must use `./gradlew auto:run`

- Now the application is running and the endpoints can be called at `127.0.0.1:4567/<one-of-4-endpoints>`. Example: http://127.0.0.1:4567/outgoing-sampleapp

To use a different configuration file to run the sample application you can use the `ADOT_JAVA_SAMPLE_APP_CONFIG` environment variable. Set this environment variable to the path of your custom configuration file.

Obs: This directory also contains `collector-config.yaml` file that can be used with the AWS Distro for OpenTelemetry collector.

https://github.com/aws-observability/aws-otel-collector

### Creating an image of the application

In order to create docker images for the auto and manual instrumentations, please follow the steps:

- Clone the repository
  `git clone https://github.com/aws-observability/aws-otel-community.git`
- Switch into the directory
  `cd sample-apps/java-sample-app`
- Run the gradle jib command
  * `./gradlew jibDockerBuild`

This will build the docker images and push it to your local docker daemon.

### Correlation between traces and logs

This application also tries to demonstrate how the correlation between traces and logs works. In order for the correlation to work, the following steps have to be followed:

1. Define the resource attribute `aws.log.group.names`. [Reference](https://opentelemetry.io/docs/reference/specification/resource/semantic_conventions/cloud_provider/aws/logs/)
2. Inject the trace id in the logs. The following string must be present in the log line `AWS-XRAY-TRACE-ID: TraceID@EntityID`. Example: `AWS-XRAY-TRACE-ID: 1-5d77f256-19f12e4eaa02e3f76c78f46a@1ce7df03252d99e1`. [Reference](https://docs.aws.amazon.com/xray/latest/devguide/xray-sdk-java-configuration.html#xray-sdk-java-configuration-logging).

Both the manual and auto instrumentation applications are implementing the steps bellow. They use different mechanisms to do that:

1. Resource attribute. This is configurable using the system property `adot.sampleapp.loggroup`. The default value is `sample-app-trace-logs`. Each application type use a different mechanism to set this resource attribute.
  * Manual instrumentation - During SDK initialization.
  * Auto instrumentation - Using extension mechanism defined [here](https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/examples/extension).
2. Trace id injection.
  * Both applications use log4j Mapped Diagnostic Context (MDC). The format of the log is defined in the `log4j2.xml` file in each application directory.

We are also including an example configuration file in `cw-agent.json` so that cloudwatch agent can capture the logs of the sample application when running locally.