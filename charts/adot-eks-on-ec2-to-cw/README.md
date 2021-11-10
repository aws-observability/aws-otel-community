# ADOT Helm chart for EKS on EC2 metrics and logs to CW Container Insights
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

The repository contains a [Helm](https://helm.sh/) chart to provide easy to operate, end-to-end  [AWS Elastic Kubernetes Service](https://aws.amazon.com/eks/) (EKS) on [AWS Elastic Compute Cloud](https://aws.amazon.com/ec2/) (EC2) monitoring with [AWS Distro for OpenTelemetry(ADOT) collector](https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/Container-Insights-EKS-otel.html) for metrics and [Fluent Bit](https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/Container-Insights-setup-logs-FluentBit.html) for logs.
Therefore, this Helm chart is useful for customers who use EKS on EC2 and want to collect metrics and logs to send to Amazon CloudWatch Container Insights.

The Helm chart configured in this repository deploys ADOT Collector and Fluent Bit as DaemonSets and is ready to collect metrics and logs and send them to Amazon CloudWatch Container Insights.

## Helm Chart Structure
```console
adot-eks-on-ec2-to-cw/
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
|-- scripts/ 
|   |-- install-tools.sh
|   |-- lint-charts.sh
|   |-- validate-charts.sh
|-- .helmignore
|-- Chart.yaml
|-- values.schema.json
|-- values.yaml
```

`templates` folder contains two subfolders, `aws-for-fluent-bit` and `aws-otel-collector`, and each subfolder contains template files that will be evaluated with the default values configured in `values.yaml.`

`script` folder contains shell script files to run chart validation and lint tests with [Helm](https://helm.sh/), [Kubeval](https://kubeval.instrumenta.dev/), and [BATS](https://bats-core.readthedocs.io/en/stable/index.html) (bash automated testing system).

`values.yaml` file stores parameterized template defaults in the Helm chart. Using this file, we can provide more flexibility to our users to expose configuration that can be overriden at installation and upgrade time.

`values.schema.json` file contains schemas of each values in values.yaml. It defines each values’ type, required keys, and constraints.



## Prerequisite

You are required to have following items in order to install this Helm chart.

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
aws-cloudwatch-metrics   adot-collector-daemonset-7nrst   1/1     Running   0          4s
aws-cloudwatch-metrics   adot-collector-daemonset-x7n8x   1/1     Running   0          4s
```

If you see these four running pods, two for Fluent Bit and two for ADOT Collector as DaemonSets within the specified namespaces, they are successfully deployed.  

### Verify whether the metrics and logs were sent to Amazon CloudWatch

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

## Configuration
To see all configurable options with detailed comments:

```console
$ helm show values [REPO_NAME]/adot-eks-on-ec2-to-cw
```

By changing values in `values.yaml`, you are able to customize the chart to use your preferred configuration.

Following options are some useful configurations that can be applied to this Helm chart.

### Deploy ADOT Collector as Sidecar

Sidecar is a microservice design pattern where a companion service runs next to your primary microservice, augmenting its abilities or intercepting resources it is utilizing. If you want to monitor in a single application, then the sidecar pattern would be the best fit. Use `helm install` command from [Install Chart](#install-chart) to deploy ADOT Collector and Fluent Bit as DaemonSet.
However, ADOT Collector can be deployed as Sidecar with the following command.

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

### Deploy Prometheus

Please refer to [deployment template](https://github.com/aws-observability/aws-otel-collector/blob/main/deployment-template/eks/otel-container-insights-prometheus.yaml) to configure ADOT Collector with Prometheus Receiver for AWS Container Insights on EKS with [configurations](https://github.com/aws-observability/aws-otel-collector/blob/main/config/eks/prometheus/config-all.yaml) in the Helm chart.

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