# Centralized Sampling Integration Tests

## Introduction

This project is meant to be used to test that X-ray's centralized sampling strategies
are working properly. This folder has sample apps configured for the integration tests
under sample apps in languages that support centralized sampling. As of now the languages
supported are Java and Go. To run these tests, first start up the collector, then
start up the chosen sample app, then start running the tests.

## Run Locally

### Set up collector
To run locally first set up the collector with the correct configuration.
It is possible that this is already done. Available ADOT Collector releases can be found here
[aws-otel-collector](https://github.com/aws-observability/aws-otel-collector/releases).
Make sure that the collector config is configured to work with a local x-ray listener pointed
to port 2000 and the docker run command exposes port 2000. Set up the ADOT collector with the 
example-collector-config file. Clone the ADOT Collector repo and start the Collector with commands.
```shell
    cd aws-otel-collector
```
```shell
    docker run --rm -p 2000:2000 -p 55680:55680 -p 8889:8888 \
      -e AWS_REGION=us-west-2 \
      -e AWS_PROFILE=default \
      -v ~/.aws:/root/.aws \
      -v "${PWD}/examples/docker/config-test.yaml":/otel-local-config.yaml \
      --name awscollector public.ecr.aws/aws-observability/aws-otel-collector:latest \
      --config otel-local-config.yaml;
```

### Start up sample app
Start up the sample app of your choice in the sample apps folder. The sample apps exist in the sample-apps folder. 
Each sample app will have a readMe on how to run it. If adding a sample-app to use for the integration tests see
[Sample-app-requirements](https://docs.google.com/document/d/1nu6XwYKe8h3EZ6upCQqf83hI9gQ-yg5WXlxHRjJ7BCg/edit?usp=sharing)
. The sample apps were manually instrumented for X-Ray Remote Sampling, for more context see
[here](https://aws-otel.github.io/docs/getting-started/java-sdk/trace-auto-instr#using-x-ray-remote-sampling)

### Start integration tests
Run this command in the  directory `centralized-sampling-tests` once the Collector 
and sample app are up and running. Ensure that the AWS account being used on your local account has no 
pre-existing sample rules in it or the tests will fail.
```shell
./gradlew :integration-tests:run
```

### Github Actions
The tests will run automatically on PRs for changes that involve the centralized-sampling-tests folder.
It is also possible to run the tests manually as a GitHub action. To do this, you will need to make a fork
of the repository. From your fork, add a GitHub secret AWS_CENTRALIZED_SAMPLING_ROLE and add an AWS Dev
account with permissions to AWS XRay and GitHub setup. 
Example: 
```shell
AWS_CENTRALIZED_SAMPLING_ROLE: arn:aws:iam::123456789012:role/S3Access
```
See [Setup AWS GitHub](https://docs.github.com/en/actions/deployment/security-hardening-your-deployments/configuring-openid-connect-in-amazon-web-services)
for more information. Once the secret is added go to your forks actions, select the Centralized Sampling Integration Tests workflow and
click the Run workflow button.

## Add a new sample app to the Centralized Sampling Integration Tests workflow

1. Make sure that a Dockerfile is associated with the sample app in the `./sample-apps` directory. Similarly to the existing Dockerfiles, it should run the sample app on port 8080.
2. Create a `docker-compose-<language>-app.yml` file in this directory. It should be the same as the existing docker-compose files, except the `services.app.build.context` value should point to the new sample app Dockerfile.
3. Add a new job in the `.github/workflows/centralized-sampling-tests.yml` workflow, similar to the existing jobs. The steps may need to be modified to use the new sample app and remove the previous sample app. It will need to use the new `docker-compose-<language>-app.yml` file as well. For the sample app integration tests to run in parallel (and not interfere with each other), the `AWS_REGION` environment variable will need to be different than the other jobs.
