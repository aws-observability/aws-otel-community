# Sample Apps


**List of all updated Standardized SDK Language sample apps in this repository**

|Sample App                   |Location                                                                                                                                    |App functionality                                                                                                                              |
|-----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
|Python Auto Instrumentation Sample App |[aws-otel-community](https://github.com/aws-observability/aws-otel-community/tree/master/sample-apps/python-auto-instrumentation-sample-app)          |Standardized integration of ADOT components for X-Ray (Traces) and EMF Exporter (Metrics) with Auto instrumentation of OpenTelemetry Python                                          |
|Python Manual Instrumentation Sample App |[aws-otel-community](https://github.com/aws-observability/aws-otel-community/tree/master/sample-apps/python-manual-instrumentation-sample-app)        |Standardized integration of ADOT components for X-Ray (Traces) and EMF Exporter (Metrics) with Manual instrumentation of OpenTelemetry Python                                        |
|Go Sample App |[aws-otel-community](https://github.com/aws-observability/aws-otel-community/tree/master/sample-apps/go-sample-app)        |Standardized integration of ADOT components for X-Ray (Traces) and EMF Exporter (Metrics) with Manual instrumentation of OpenTelemetry Go                                        |
|Javascript Sample App |[aws-otel-community](https://github.com/aws-observability/aws-otel-community/tree/master/sample-apps/javascript-sample-app)        |Standardized integration of ADOT components for X-Ray (Traces) and EMF Exporter (Metrics) with Manual instrumentation of OpenTelemetry Javascript                                  |
|Java Auto Instrumentation Sample App |[aws-otel-community](https://github.com/aws-observability/aws-otel-community/tree/master/sample-apps/java-sample-app/auto)        |Standardized integration of ADOT components for X-Ray (Traces) and EMF Exporter (Metrics) with Auto instrumentation of OpenTelemetry Java                                  |
|Java Manual Instrumentation Sample App |[aws-otel-community](https://github.com/aws-observability/aws-otel-community/tree/master/sample-apps/java-sample-app/manual)        |Standardized integration of ADOT components for X-Ray (Traces) and EMF Exporter (Metrics) with Manual instrumentation of OpenTelemetry Java                                  |
|.NET Manual Instrumentation Sample App |[aws-otel-community](https://github.com/aws-observability/aws-otel-community/tree/master/sample-apps/dotnet-sample-app)       |Standardized integration of ADOT components for X-Ray (Traces) and EMF Exporter (Metrics) with Manual instrumentation of OpenTelemetry .NET                                  |

**How to run these standardized sample apps?**
Each of these standardized sample apps comes with a README attached inside the repository that defines how to build and run the sample app locally to produce traces and metrics to a certain endpoint.  Each sample app (except the Java sample app) also includes a Dockerfile that can be use to build a docker image of the sample app.  The Java sample app specifically uses `./gradlew jibDockerBuild` in order to build the images for the respective instrumentation sample apps. More details about what each sample apps does can be found in the [Sample App Spec](#sample-app-specification) linked below.

**SDK Language stability matrix** 

|Language                   |Traces                                                                                                                                    |Metrics                                                                                                                              |
|-----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
|Python |Stable         |Stable                    |
|Javascript |Stable       |Stable                         |
|Go |Stable         |Beta                    |
|Java |Stable       |Stable                         |
|.NET |Stable         |Stable                    |

**List of all non-SDK sample apps across all repositories in [aws-observability](https://github.com/aws-observability) org.**

|Sample App                   |Location                                                                                                                                    |App functionality                                                                                                                              |Language  |
|-----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|----------|
|Prometheus-sample-app        |[aws-otel-community](https://github.com/aws-observability/aws-otel-community/tree/master/sample-apps/prometheus-sample-app)                 |Generates prometheus's metrics (counter, gauge, histogram,summary)                                                                             |Go        |  
|Jmx                          |[aws-otel-test-framework](https://github.com/aws-observability/aws-otel-test-framework/tree/terraform/sample-apps/jmx)                      |Generates prometheus metrics                                                                                                                   |Java      |
|Jaeger-Zipkin                |[aws-otel-test-framework](https://github.com/aws-observability/aws-otel-test-framework/tree/terraform/sample-apps/jaeger-zipkin-sample-app) |Emits trace data using zipkin and jaeger                                                                                                       |Java      |
|Statsd                       |[aws-otel-testframework](https://github.com/aws-observability/aws-otel-test-framework/tree/terraform/sample-apps/statsd)                    |Emits metrics in statsd format                                                                                                                 |Python    |
|Prometheus sample app        |[aws-otel-test-framework](https://github.com/aws-observability/aws-otel-test-framework/tree/terraform/sample-apps/prometheus)               |Generates prometheus's metrics (counter, gauge, histogram,summary)                                                                             |Go        |

**Java instrumentation specific sample apps**

|Sample App                   |Location                                                                                                                                    |App functionality                                                                                                                              |
|-----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
|Spark-awssdk1                |[aws-otel-java-instrumentation](https://github.com/aws-observability/aws-otel-java-instrumentation/tree/main/sample-apps/spark-awssdkv1)    |Generates OTLP metrics and traces                                                                                                              |
|Spark                        |[aws-otel-java-instrumentation](https://github.com/aws-observability/aws-otel-java-instrumentation/tree/main/sample-apps/spark)             |Generates OTLP metrics and traces                                                                                                              |
|Springboot                   |[aws-otel-java-instrumentation](https://github.com/aws-observability/aws-otel-java-instrumentation/tree/main/sample-apps/springboot)        |Generates OTLP metrics and traces                                                                                                              |

**Python instrumentation sample apps (to be deprecated)**

|Sample App                   |Location                                                                                                                                    |App functionality                                                                                                                              |
|-----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
|Python-auto instrumentation  |[aws-otel-python](https://github.com/aws-observability/aws-otel-python/tree/main/integration-test-apps/auto-instrumentation/flask)          |Continuous integration of ADOT components for X-Ray with Auto instrumentation of OpenTelemetry Python                                          |
|Python-manual instrumentation|[aws-otel-python](https://github.com/aws-observability/aws-otel-python/tree/main/integration-test-apps/manual-instrumentation/flask)        |Continuous integration of ADOT components for X-Ray with manual instrumentation of OpenTelemetry Python                                        |
|Python-none instrumentation  |[aws-otel-python](https://github.com/aws-observability/aws-otel-python/tree/main/integration-test-apps/none-instrumentation/flask)          |This application provides a baseline for performance testing, has no instrumentation, helps reveal the overhead that comes with instrumentation|

**Ruby instrumentation sample app**

|Sample App                   |Location                                                                                                                                    |App functionality                                                                                                                              |
|-----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
|Ruby-manual-instrumentation  |[aws-otel-community](https://github.com/aws-observability/aws-otel-community/tree/master/sample-apps/ruby-rails-sample-app)              |Continuous integration of ADOT X-Ray components and X-Ray service. Manual Instrumentation using OpenTelemetry Ruby                              |

# Sample App Specification

[Sample App Spec](SampleAppSpec.md)

