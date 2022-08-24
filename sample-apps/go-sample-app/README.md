### Application interface

This Go sample app will emit Traces and Metrics with Logs as experimental. There are two types of metrics emitted;
Request Based and Random Based.
Metrics are generated as soon as the application is ran or deployed without any additional effort. These are considered the random based metrics which track a mock of TimeAlive, TotalHeapSize, ThreadsActive and CpuUsage. The boundaries for these metrics are standard and can be found in the configuration file (YAML) called config.yaml.
Additionally, you can generate Traces and request based Metrics by making requests to the following exposed endpoints.
Due to the upstream Go SDK being unstable for metrics, we do not support metrics further than for generating values for demo purposes. 

1. /
    1. Ensures the application is running
2. /outgoing-http-call
    1. Makes a HTTP request to aws.amazon.com (http://aws.amazon.com/)
3. /aws-sdk-call
    1. Makes a call to AWS S3 to list buckets for the account corresponding to the provided AWS credentials
4. /outgoing-sampleapp
    1. Makes a call to all other sample app ports configured at `<host>:<port>/outgoing-sampleapp`. If none available, makes a HTTP request to www.amazon.com (http://www.amazon.com/) 

### Requirements

* Go 1.17+

### Running the application

For more information on running a Go application using manual instrumentation, please refer to ADOT Getting Started with the OpenTelemetry Go SDK on Traces Instrumentation (https://aws-otel.github.io/docs/getting-started/go-sdk). In this context, the ADOT Collector is being run locally as a sidecar.
By default, in the provided configuration file, the host and port are set to 0.0.0.0:8080.

In order to run the application

- Clone the repository
`git clone https://github.com/aws-observability/aws-otel-community.git`
- Switch into the directory
`cd sample-apps/go-sample-app`
- Run the go server
`go run main.go`
Now the application is ran and the endpoints can be called at `0.0.0.0:8080/<one-of-4-endpoints>`.

### Non conformance

The Otel Go SDK Metrics are not stable. Metrics are implemented in this sample app but are not to be tested against. Their primary use would be for demos and getting started guides. 
Missing resource detectors.
