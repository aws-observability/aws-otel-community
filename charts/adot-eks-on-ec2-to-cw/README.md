# ADOT Helm chart for EKS on EC2 metrics and logs to CW Container Insights
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

The repository contains a [Helm](https://helm.sh/) chart to provide easy to operate, end-to-end  [AWS Elastic Kubernetes Service](https://aws.amazon.com/eks/) (EKS) on [AWS Elastic Compute Cloud](https://aws.amazon.com/ec2/) (EC2) monitoring with [AWS Distro for OpenTelemetry(ADOT) collector](https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/Container-Insights-EKS-otel.html) for metrics and [Fluent Bit](https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/Container-Insights-setup-logs-FluentBit.html) for logs.
Therefore, this Helm chart is useful for customers who use EKS on EC2 and want to collect metrics and logs to send to Amazon CloudWatch Container Insights.

The Helm chart configured in this repository deploys ADOT Collector and Fluent Bit as DaemonSets and is ready to collect metrics and logs and send them to Amazon CloudWatch Container Insights.

## Helm Chart Structure
```console
adot-eks-on-ec2-to-cw/
|-- scripts/ 
|   |-- install-tools.sh
|   |-- lint-charts.sh
|   |-- validate-charts.sh
|-- templates/
|   |-- NOTES.txt
|   |-- aws-for-fluent-bit/
|   |   |-- _helpers.tpl
|   |   |-- clusterrole.yaml
|   |   |-- clusterrolebinding.yaml
|   |   |-- configmap.yaml
|   |   |-- daemonset.yaml
|   |   |-- namespace.yaml
|   |   |-- serviceaccount.yaml
|   |-- aws-otel-collector/
|   |   |-- _helpers.tpl
|   |   |-- clusterrole.yaml
|   |   |-- clusterrolebinding.yaml
|   |   |-- configmap.yaml
|   |   |-- daemonset.yaml
|   |   |-- namespace.yaml
|   |   |-- serviceaccount.yaml
|   |   |-- sidecar.yaml
|   |   |-- sidecarnamespace.yaml
|-- Chart.yaml
|-- values.schema.json
|-- values.yaml
```

`templates` folder contains two subfolders, `aws-for-fluent-bit` and `aws-otel-collector`, and each subfolder contains template files that will be evaluated with the default values configured in `values.yaml.`

`script` folder contains shell script files to run chart validation and lint tests with [Helm Lint](https://helm.sh/docs/helm/helm_lint/) and [Kubeval](https://kubeval.instrumenta.dev/).

`values.yaml` file stores parameterized template defaults in the Helm chart. Using this file, we can provide more flexibility to our users to expose configuration that can be overriden at installation and upgrade time.

`values.schema.json` file contains schemas of each values in values.yaml. It defines each values’ type, required keys, and constraints.

`_helpers.tpl` files are used to define GO template helpers to create name variables.

## Prerequisite

The following pre-requisites need to be set up and installed in order to install this Helm chart.

- EKS Cluster on EC2
- IAM Role
- Helm v3+

## Get Repository Information

[Helm](https://helm.sh/) must be installed to use the chart. Please refer to Helm's [documentation](https://helm.sh/docs/) to get started.

Once Helm is set up properly, add this repo as follows:
```console
$ helm repo add [REPO_NAME] https://TO_BE_RELEASED.github.io/adot-helm-eks-ec2
$ helm search repo [REPO_NAME] # Run this command in order to see the charts.
```

### Verify the Helm chart works as expected
- Run chart validation test and lint from`MakeFile`.
```console
$ cd adot-eks-on-ec2-to-cw
$ make install-tools # required initially
$ make all           # to run chart validation test and lint 
```

## Install Chart

```console
$ helm install \
  [RELEASE_NAME] [REPO_NAME]/adot-eks-on-ec2-to-cw \
  --set clusterName=[CLUSTER_NAME] --set awsRegion=[AWS_REGION]
```
`CLUSTER_NAME` and `AWS_REGION` must be specified with your own EKS cluster and the region.
You can find these values by executing following command.

```console
$ kubectl config current-context

[IAM_User_Name]@[CLUSTER_NAME].[AWS_REGION].eksctl.io
```

To verify the installation is successful, you can execute the following command.

```console
$ kubectl get pods --all-namespaces

NAMESPACE                NAME                             READY   STATUS    RESTARTS   AGE
amazon-cloudwatch        fluent-bit-f27cz                 1/1     Running   0          4s
amazon-cloudwatch        fluent-bit-m2mkr                 1/1     Running   0          4s
amzn-cloudwatch-metrics  adot-collector-daemonset-7nrst   1/1     Running   0          4s
amzn-cloudwatch-metrics  adot-collector-daemonset-x7n8x   1/1     Running   0          4s
```

If you see these four running pods, two for Fluent Bit and two for ADOT Collector as DaemonSets within the specified namespaces, they are successfully deployed.

### Verify the metrics and logs are sent to Amazon CloudWatch

- Open Amazon CloudWatch console
- Select "Logs -> Log groups" on the left navigation bar.
- Check if following four log groups exist (performance log group will take longer than others).
```console
/aws/containerinsights/[CLUSTER_NAME]/application
/aws/containerinsights/[CLUSTER_NAME]/dataplane
/aws/containerinsights/[CLUSTER_NAME]/host
/aws/containerinsights/[CLUSTER_NAME]/performance
```
- Select "Insights -> Container Insights" on the left navigation bar.
- Choose Performance monitoring in the drop-down menu on the top-left side.
- Choose the levels such as EKS pods, EKS nodes, and EKS namespaces from the drop-down menu in the automated dashboard.
- If you observe metrics of the running pods for CPU Utilization, Memory Utilization, etc, the metrics are successfully collected and visualized in Container Insights.

![CWCI_dashboard](https://user-images.githubusercontent.com/38146012/141032708-9080ed8a-ff68-4227-8ea5-98c8fd5deff8.jpeg)

## Configuration
To see all configurable options with detailed comments:

```console
$ helm show values [REPO_NAME]/adot-eks-on-ec2-to-cw
```

By changing values in `values.yaml`, you are able to customize the chart to use your preferred configuration.

Following options are some useful configurations that can be applied to this Helm chart.

### Deploy ADOT Collector as Sidecar

Sidecar is a microservice design pattern where a companion service runs next to your primary microservice, augmenting its abilities or intercepting resources it is utilizing. The sidecar pattern would be the best fit for a single application monitoring.
In order to deploy the ADOT Collector in Sidecar mode using the Helm chart, 1) update `sidecar.yaml` and `values.yaml` files in the Helm chart with the application configurations and 2) include the use of `--set` flag in the `helm install` command from [Install Chart](#install-chart).

```console
$ helm install \
  [RELEASE_NAME] [REPO_NAME]/adot-eks-on-ec2-to-cw \
  --set clusterName=[CLUSTER_NAME] --set awsRegion=[AWS_REGION] \
  --set adotCollector.daemonSet.enabled=false --set adotCollector.sidecar.enabled=true
```
The use of `--set` flag with `enabled=true` or `enabled=false` can switch on/off the specified deployment mode. The command set `enabled=false` for ADOT Collector as DaemonSet and `enabled=true` to deploy ADOT Collector as Sidecar.
You can also check whether your applications are successfully deployed by executing the following command.

```console
$ kubectl get pods --all-namespaces

NAMESPACE                NAME                            READY   STATUS    RESTARTS   AGE
adot-sidecar-namespace   adot-sidecar-658dc9ffbb-w9zv2   2/2     Running   0          5m18s
amazon-cloudwatch        fluent-bit-9dcql                1/1     Running   0          5m18s
amazon-cloudwatch        fluent-bit-wqhmd                1/1     Running   0          5m18s
```

### Deploy ADOT Collector as Deployment and StatefulSet

Deploying ADOT Collector as Deployment and StatefulSet mode requires installing ADOT Operator. See [OpenTelemetry Operator Helm Chart](https://github.com/open-telemetry/opentelemetry-helm-charts/tree/main/charts/opentelemetry-operator) for detailed explanation.


### Deploy ADOT Collector with Prometheus Receiver for AWS Container Insights on EKS

Please refer to [deployment template](https://github.com/aws-observability/aws-otel-collector/blob/main/deployment-template/eks/otel-container-insights-prometheus.yaml) to deploy ADOT Collector with [Prometheus Receiver](https://github.com/open-telemetry/opentelemetry-collector-contrib/blob/d46048ac4dd01062c803867cb6a13377ea287a23/receiver/prometheusreceiver/README.md#prometheus-receiver) and [Amazon CloudWatch Embedded Metric Format (EMF) Exporter](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/exporter/awsemfexporter#aws-cloudwatch-emf-exporter-for-opentelemetry-collector) for AWS Container Insights on EKS 
via [configurations](https://github.com/aws-observability/aws-otel-collector/blob/main/config/eks/prometheus/config-all.yaml) in the Helm chart.

### AWS EKS on Fargate 
The prerequisites for  [Fargate logging](https://docs.aws.amazon.com/eks/latest/userguide/fargate-logging.html) via Amazon EKS on [AWS Fargate](https://docs.aws.amazon.com/eks/latest/userguide/fargate.html) include: 1) [Create a Fargate profile for your cluster](https://docs.aws.amazon.com/eks/latest/userguide/fargate-getting-started.html#fargate-gs-create-profile) 
and 2) [Create a Fargate pod execution role](https://docs.aws.amazon.com/eks/latest/userguide/fargate-getting-started.html#fargate-sg-pod-execution-role). <br>

Amazon EKS on Fargate features a Fluent Bit based built-in log router to send collected logs to various destinations, including Amazon CloudWatch.
Fargate utilizes [AWS for Fluent Bit](https://github.com/aws/aws-for-fluent-bit), 
and the required configurations for Fargate to automatically detect and configure the log router are included in the Helm chart in `configmap.yaml` and `values.yaml` files based on the [Fargate logging](https://docs.aws.amazon.com/eks/latest/userguide/fargate-logging.html) user guide.
The configurations in `configmap.yaml` must include the name: `aws-logging` and the namespace: `aws-observability` for Fargate logging. To deploy your application to Amazon EKS on Fargate, you need to include your application yaml file 
in the `aws-fargate-logging` folder of the Helm chart with the same namespace as your [AWS Fargate profile](https://docs.aws.amazon.com/eks/latest/userguide/fargate-profile.html). For more detailed information about Fargate logging, such as deployment of a `sample-app.yaml` or your application and 
the instructions to download, create, and attach IAM policy to the [pod execution role](https://docs.aws.amazon.com/eks/latest/userguide/pod-execution-role.html) for Fargate profile, 
please refer to the user guide for [Fargate logging](https://docs.aws.amazon.com/eks/latest/userguide/fargate-logging.html) and [Getting started with AWS Fargate using Amazon EKS](https://docs.aws.amazon.com/eks/latest/userguide/fargate-logging.html).

This is an example of using the Helm chart for Fargate logging with the `sample-app.yaml` from [Fargate logging](https://docs.aws.amazon.com/eks/latest/userguide/fargate-logging.html).
```console
$ helm install \
  [RELEASE_NAME] [REPO_NAME]/adot-eks-on-ec2-to-cw \
  --set clusterName=[CLUSTER_NAME] --set awsRegion=[AWS_REGION] \
  --set fargateLogging.enabled=true
```
To confirm the `sample-app` is deployed and troubleshoot the logging is enabled/disabled, you can run the following commands.
```console
$ kubectl get pods --all-namespaces

NAMESPACE               NAME                            READY   STATUS    RESTARTS   AGE
aws-observability       sample-app-86b8cc866b-cr5x6     1/1     Running   0          13m
aws-observability       sample-app-86b8cc866b-q75z7     1/1     Running   0          13m
aws-observability       sample-app-86b8cc866b-t615c     1/1     Running   0          13m
```

```console
$ kubectl describe po -n aws-observability sample-app-86b8cc866b-cr5x6

Events:
  Type    Reason          Age  From               Message 
  ----    ------          ---  ----               -------
  Normal  LoggingEnabled  13m  fargate-scheduler  Successfully enabled logging for pod 
```
## Uninstall Chart

The following command uninstalls the chart. 
This will remove all the Kubernetes components associated with the chart and deletes the release.

```console
$ helm uninstall [RELEASE_NAME]
```

## Upgrade Chart

```console
$ helm upgrade [RELEASE_NAME] [REPO_NAME]/adot-eks-on-ec2-to-cw
```

## Contributing

See [CONTRIBUTING.md](./CONTRIBUTING.md).

## Contributors

[Hyunuk Lim](https://github.com/hyunuk)

[James Park](https://github.com/JamesJHPark)

## Further Information

[Set up Fluent Bit as a DaemonSet to send logs to CloudWatch Logs](https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/Container-Insights-setup-logs-FluentBit.html)

[Using AWS Distro for OpenTelemetry](https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/Container-Insights-EKS-otel.html)


## License

<!-- Keep full URL links to repo files because this README syncs from main to gh-pages.  -->
[Apache 2.0 License](https://github.com/prometheus-community/helm-charts/blob/main/LICENSE).

## Support Plan

Our team plans to fully support the code we plan to release in this repo.
