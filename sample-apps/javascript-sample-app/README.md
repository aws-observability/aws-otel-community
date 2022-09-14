## Javascript Opentelemetry Sample App

### Description
This is a Javascript auto-instrumented sample app demonstrating the features of OTel. Tracing is automatically instrumented, but there are also some aspects of manual instrumentation involved as well.

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
