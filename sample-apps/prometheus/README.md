# Prometheus sample app

This Prometheus sample app generates all 4 Prometheus metric types (counter, gauge, histogram, summary) and exposes them at the `/metrics` endpoint

A health check endpoint also exists at `/`

The following is a list of optional command line flags for configuration:
* `listen_address`: (default = `0.0.0.0:8080`)this defines the address and port that the sample app is exposed to. This is primarily to conform with the test framework requirements.
* `metric_count`: (default=1) the amount of each type of metric to generate. The same amount of metrics is always generated per metric type.

Steps for running locally:
```bash
$ go build .
$ ./prometheus_sample_app -listen_address=0.0.0.0:4567 -metric_count=100
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
$ curl localhost:9001/metricss
```
