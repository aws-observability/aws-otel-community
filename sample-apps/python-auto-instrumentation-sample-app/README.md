## Python Opentelemetry Auto-instrumentation Sample App

### Description

This Python Sample App will emit Traces and Metrics. There are two types of metrics emitted;
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

There are two type of Python sample application that expose the exact same metrics and endpoints:

* Auto - No code is necessary to instrument supported third party libraries and the initialization of opentelemetry is done through system properties.
* Manual - All setup needs to be done explicitly using Java code.
This application uses Auto instrumentation

[Sample App Spec](../SampleAppSpec.md)

* Non-conformance: This SDK language is not missing any features or extensions required other than Resource Detectors other than Resource Detectors
* Workarounds: No workarounds are being used in this application

### Getting Started:

#### Running the application (Local)

In order to run the application

- Create venv
`python3 -m venv .` and
`source bin/activate`
- Install requirements
`pip install --no-cache-dir -r requirements.txt`
- Run app with environment variables set
`OTEL_RESOURCE_ATTRIBUTES='service.name=python-auto-instrumentation-sample-app' OTEL_PROPAGATORS=xray OTEL_PYTHON_ID_GENERATOR=xray opentelemetry-instrument python app.py`
Now the application is ran and the endpoints can be called at `0.0.0.0:8080/<one-of-4-endpoints>`.

#### Docker

In order to build the Docker image and run it in a container

- Build the image
`docker build -t python-auto-instrumentation-sample-app .`
- Run the image in a container
`docker run -p 8080:8080 python-auto-instrumentation-sample-app`

