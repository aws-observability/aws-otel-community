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

Obs: This directory also contains `config.yaml` file that can be used with the AWS Distro for OpenTelemetry collector.

https://github.com/aws-observability/aws-otel-collector
