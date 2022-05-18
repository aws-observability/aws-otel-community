# ADOT Preview Program

Start here to participate in the AWS Distro for OpenTelemetry (ADOT) Preview Program. 
This program covers all preview features and AWS services integrations available for ADOT.

## Scope

What’s *not in scope* for this program:

* The [default tracing pipelines](https://github.com/aws-observability/aws-otel-collector/blob/main/config.yaml), which includes trace collection for X-Ray.
* Metrics support 
  * Includes the ADOT Collector and collection pipelines using [Prometheus](https://aws-otel.github.io/docs/getting-started/advanced-prometheus-remote-write-configurations) components 
  to send metrics to [Amazon Managed Service for Prometheus](https://aws-otel.github.io/docs/getting-started/prometheus-remote-write-exporter) 
  and [CloudWatch Metrics](https://aws-otel.github.io/docs/getting-started/cloudwatch-metrics#cloudwatch-emf-exporter-awsemf).
  * Java, .NET, and Python SDK support
* Any partner components and integrations.
* The following [components](https://aws-otel.github.io/docs/releases) of ADOT:
    * AWS X-Ray Playground for OpenTelemetry
    * AWS Distro for OpenTelemetry Integration Test Framework
* The following ADOT collector pipeline components are not in scope for this program (since they are already GA): `awsxrayreceiver` and `awsxrayexporter`.

What *is included* in this program:

* Metrics support for the following SDKs
  * Go, Ruby, JavaScript
* Logs support, which includes collection pipelines using component to send logs to [CloudWatch Logs](https://aws-otel.github.io/docs/getting-started/cloudwatch-metrics#cloudwatch-emf-exporter-awsemf).
* The following [components](https://aws-otel.github.io/docs/releases):
    * AWS Distro for OpenTelemetry [Collector](https://aws-otel.github.io/docs/getting-started/collector)
    * AWS Distro for OpenTelemetry [Operator](https://aws-otel.github.io/docs/getting-started/operator)
* The following ADOT collector pipeline components are in scope:
    * receivers: `awsecscontainermetricsreceiver`, `statsdreceiver`, and `awscontainerinsightreceiver`
    * exporters: `awsemfexporter` and `awsprometheusremotewriteexporter`
    * extension: `ecsobserver` and `awsproxy`

## Expectations

The motivation for this program is to gather feedback from customers in a structured manner. We want to learn about use cases, 
UX issues, and bug reports as it pertains to the components included in the program.

As an ADOT Preview Program participant, please consider:

* This is  self-service program. This means that we provide support on a best-effort basis via [GitHub issues](https://github.com/aws-observability/aws-otel-community/issues) and responses on the issues in a timely manner.
* Use the components included in the program at your own risk, we do not assume liability for any disruptions or costs caused by ADOT Preview Program components.
* We do not provide guarantees as to stability or performance. Since you may lose data or experience and impact on other components or services, 
  we strongly advise you are only using the ADOT Preview Program components in a dev/test environment, isolated from your production environment.
* Preview programs are exclusively not included in AWS Enterprise support. This means, any issues you run into should be reported via the respective components
  repositories on GitHub.

## Artifacts 

You can either build container images for the ADOT collector and the ADOT operator yourself or obtain 
them via the [ECR Public Gallery](https://gallery.ecr.aws/aws-observability/), the official source for all our container images.

Further we provide the following artifacts:

* Configurations:
    * All Collector configurations are available on our ADOT GitHub repo [aws-observability/aws-otel-collector](https://github.com/aws-observability/aws-otel-collector/tree/main/config).
    * GA tracing SDKs for [Java](https://opentelemetry.io/docs/java/), [JavaScript](https://opentelemetry.io/docs/js/), 
      [Python](https://opentelemetry.io/docs/python/), [.NET](https://opentelemetry.io/docs/net/), and [Go](https://opentelemetry.io/docs/go/).
* For Kubernetes and specifically EKS, we provide Helm charts via the [aws-observability/aws-otel-helm-charts](https://github.com/aws-observability/aws-otel-helm-charts) repo.
* Security policies and good practice:
    * [IAM policies](https://aws-otel.github.io/docs/setup/permissions) and [SigV4 support](https://docs.aws.amazon.com/general/latest/gr/signature-version-4.html) 
      for accessing destinations such as Amazon Managed Service for Prometheus or CloudWatch.
    * For Kubernetes/EKS we strongly recommend to use [IAM roles for service accounts](https://docs.aws.amazon.com/eks/latest/userguide/iam-roles-for-service-accounts.html).
    * [Pod execution roles for EKS on Fargate](https://docs.aws.amazon.com/eks/latest/userguide/fargate-getting-started.html#fargate-sg-pod-execution-role) 
      and [task execution roles for ECS on Fargate](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/task_execution_IAM_role.html).

## References

* [AWS observability recipes](https://aws-observability.github.io/aws-o11y-recipes/)
* [ADOT tech docs](http://aws-otel.github.io/documentation)
* AWS Docs for container compute [ECS](https://docs.aws.amazon.com/ecs/) and [EKS](https://docs.aws.amazon.com/eks/)
* AWS Docs for monitoring services:
    * [Amazon CloudWatch (X-Ray)](https://docs.aws.amazon.com/cloudwatch/)
    * [Amazon Managed Grafana](https://docs.aws.amazon.com/grafana/)
    * [Amazon Managed Service for Prometheus](https://docs.aws.amazon.com/prometheus/)
