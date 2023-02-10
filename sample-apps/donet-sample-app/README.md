### Application interface

This .NET sample app will emit Traces and Metrics with Logs as experimental. There are two types of metrics emitted;
Request Based and Random Based.
Metrics are generated as soon as the application is ran or deployed without any additional effort. These are considered the random based metrics which track a mock of TimeAlive, TotalHeapSize, ThreadsActive and CpuUsage. The boundaries for these metrics are standard and can be found in the configuration file (YAML) called config.yaml.
Additionally, you can generate Traces and request based Metrics by making requests to the following exposed endpoints.
 

1. /
    1. Ensures the application is running
2. /outgoing-http-call
    1. Makes a HTTP request to aws.amazon.com (http://aws.amazon.com/)
3. /aws-sdk-call
    1. Makes a call to AWS S3 to list buckets for the account corresponding to the provided AWS credentials
4. /outgoing-sampleapp
    1. Makes a call to all other sample app ports configured at `<host>:<port>/outgoing-sampleapp`. If none available, makes a HTTP request to www.amazon.com (http://www.amazon.com/) 

### Requirements

* .NET 6.0

### Running the application


In order to run the application

- Clone the repository
`git clone https://github.com/aws-observability/aws-otel-community.git`
- Switch into the directory
`cd sample-apps/donet-sample-app`
- Run the go server
`docker-compose up`
Now the application is ran and the endpoints can be called at `0.0.0.0:8080/<one-of-4-endpoints>`.

### Non conformance

None
