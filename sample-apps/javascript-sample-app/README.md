## Javascript Opentelemetry Sample App

### Description

This Javascript Sample App will emit Traces and Metrics. There are two types of metrics emitted;
Request Based and Random Based.
Metrics are generated as soon as the application is ran or deployed without any additional effort. These are considered the random based metrics which track a mock of TimeAlive, TotalHeapSize, ThreadsActive and CpuUsage. The boundaries for these metrics are standard and can be found in the configuration file (YAML) called config.yaml.

Additionally, you can generate Traces and request based Metrics by making requests to the following exposed endpoints:

1. /
    1. Ensures the application is running
2. /outgoing-http-call
    1. Makes a HTTP request to aws.amazon.com (https://aws.amazon.com/)
3. /aws-sdk-call
    1. Makes a call to AWS S3 to list buckets for the account corresponding to the provided AWS credentials
4. /outgoing-sampleapp
    1. Makes a call to all other sample app ports configured at `<host>:<port>/outgoing-sampleapp`. If none available, makes a HTTP request to www.amazon.com (https://www.amazon.com/)

There are two type of Java sample application that expose the exact same metrics and endpoints:

* Auto - No code is necessary to instrument supported third party libraries and the initialization of opentelemetry is done through system properties.
* Manual - All setup needs to be done explicitly using Java code.


### Getting Started:

##### Local

```
npm install

node server.js
```
#### Docker

```
docker build -t javascript-sample-app .

docker run -p 8080:8080 javascript-sample-app
```
