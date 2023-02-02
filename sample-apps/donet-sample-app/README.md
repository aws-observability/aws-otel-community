# AWS Distro for OpenTelemetry .NET - Integration Testing App

This application validates the continual integration with the AWS Distro for OpenTelemetry .NET and the AWS X-Ray back-end service. Validation is done using the [AWS Test Framework for OpenTelemetry](https://github.com/aws-observability/aws-otel-test-framework).

## Application interface

The application exposes the following routes:
1. `/`
    - Ensures the application is running.
2. `/outgoing-http-call`
    - Makes a HTTP request to `aws.amazon.com`.
3. `/aws-sdk-call`
    - Makes a call to AWS S3 to list buckets for the account corresponding to the provided AWS credentials.

## Running the integration testing application locally

This application **lacks** dependencies for AWS X-Ray trace id generator, propagator and AWS client instrumentation and is intended for github workflow. If you want to run it locally, follow steps below:

1. Checkout `aws-otel-dotnet` repo and navigate to the `integration-test-app/` folder.

2. In the same folder, replace the `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` values with your AWS credentials in `docker-compose.yml` file and run:

```shell
docker build -t aspnetapp .
docker-compose up
```

3. Visit the following endpoints when containers start:

`localhost:8080/aws-sdk-call` and `localhost:8080/outgoing-http-call`

You should be able to see traces in X-Ray console in your account(`us-west-2`).
