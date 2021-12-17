# prometheus-sample-app

This Prometheus sample app generates all 4 Prometheus metric types (counter, gauge, histogram, summary) and exposes them at the `/metrics` endpoint

A health check endpoint also exists at `/`

The following is a list of optional command line flags for configuration:
* `listen_address`: (default = `0.0.0.0:8080`)this defines the address and port that the sample app is exposed to. This is primarily to conform with the test framework requirements.
* `metric_count`: (default=1) the amount of each type of metric to generate. The same amount of metrics is always generated per metric type.
* `label_count`: (default=1) the amount of labels per metric to generate.
* `datapoint_count`: (default=1) the number of data-points per metric to generate. 

Steps for running locally:
```bash
$ go build .
$ ./prometheus-sample-app -listen_address=0.0.0.0:4567 -metric_count=100
```

Steps for running in docker:

```bash
$ docker build . -t prometheus-sample-app
$ docker run -it -p 8080:8080 prometheus-sample-app /bin/main -listen_address=0.0.0.0:8080
$ curl localhost:8080/metrics
```

Note that the port in LISTEN_ADDRESS must match the the second port specified in the port-forward

More functioning examples:

```bash
$ docker build . -t prometheus-sample-app
$ docker run -it -p 9001:8080 prometheus-sample-app /bin/main -listen_address=0.0.0.0:8080
$ curl localhost:9001/metrics
```

```bash
$ docker build . -t prometheus-sample-app
$ docker run -it -p 9001:8080 prometheus-sample-app /bin/main -listen_address=0.0.0.0:8080 -metric_count=100
$ curl localhost:9001/metrics
```

Running the commands above will require a config file for setting defaults. The config file is provided in this application. To modify it just change the values.
To override config file defaults you can specify your arguments via command line

Usage of generate:

  -is_random

    	Metrics specification

  -metric_count int

    	Amount of metrics to create

  -metric_frequency int

    	Refresh interval in seconds 

  -metric_type string
  
    	Type of metric (counter, gauge, histogram, summary) 

  -label_count int

    	Amount of labels to create per metric

  -datapoint_count int

    	Number of datapoints to create per metric

Example: 
```bash
$ docker build . -t prometheus-sample-app
$ docker run -it -p 8080:8080 prometheus-sample-app /bin/main -listen_address=0.0.0.0:8080 generate -metric_type=summary -metric_count=30 -metric_frequency=10
$ curl localhost:8080/metrics
```
```bash
$ docker build . -t prometheus-sample-app
$ docker run -it -p 8080:8080 prometheus-sample-app /bin/main -listen_address=0.0.0.0:8080 generate -metric_type=all -is_random=true
$ curl localhost:8080/metrics
```

## Clustering:
Deploy the example deployment configuration of 5 instances of Prometheus-Sample-App along with configured OTEL Collector.
    
### Pre-requisites:
- Docker
- Docker Image Prometheus-Sample-App
- A Kubernetes cluster

### Deployment on Minikube:
- Run Docker
- Start Minikube
  ```bash
    $ minikube start
  ```
- Run following command to deploy: 
    ```bash
    $ kubectl apply -f otel-collector-k8s-deployment.yaml
    $ kubectl create clusterrolebinding service-reader-pod --clusterrole=service-reader --serviceaccount=default:default
    $ kubectl apply -f prometheus-sample-app-k8s-deployment.yaml
    ```
- Run following command to monitor logs from OTEL Collector Logging exporter :
    ```bash
    $ kubectl logs <otel-collector-pod-name>
    ```
### Deployment on EKS:
- Create your cluster on EKS
  ```bash
  $ eksctl create cluster --name <cluster-name> --region <region> --with-oidc --ssh-access --ssh-public-key <public-key>
  ```
- Create repository on Amazon ECR to push docker image of Prometheus-Sample-App
- Push the prometheus_sample_app docker image to this repository
    ```bash
    $ aws ecr get-login-password --region region | docker login --username AWS --password-stdin aws_account_id.dkr.ecr.region.amazonaws.com
    $ docker build -t prometheus_sample_app .
    $ docker tag prometheus_sample_app:latest aws_account_id.dkr.ecr.region.amazonaws.com/my-repository:tag
    $ docker push aws_account_id.dkr.ecr.region.amazonaws.com/my-repository:tag
    ```
- Update imagePullPolicy of 'prometheus-sample-app-k8s-deployment.yaml' to IfNotPresent
- Copy image URI from the AWS ECR repository and update in 'prometheus-sample-app-k8s-deployment.yaml'
- Run following command to deploy:
    ```bash
    $ kubectl apply -f otel-collector-k8s-deployment.yaml
    $ kubectl create clusterrolebinding service-reader-pod --clusterrole=service-reader --serviceaccount=default:default
    $ kubectl apply -f prometheus-sample-app-k8s-deployment.yaml
    ```
- Run following command to monitor logs from OTEL Collector Logging exporter :
    ```bash
    $ kubectl logs <otel-collector-pod-name>
    ```

Currently, OTEL Collector is configured with Logging exporter. In this example, all replica Prometheus-Sample-App pods will produce identical metrics, and the Prometheus Exporter doesn't ingest identical metrics (same name and label) from different sources.
