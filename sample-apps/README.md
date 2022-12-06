# Sample Apps


List of sample apps across all repositories in [aws-observability](https://github.com/aws-observability) org.

|Sample App                   |Location                                                                                                                                    |App functionality                                                                                                                              |Language  |
|-----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|----------|
|Prometheus-sample-app        |[aws-otel-community](https://github.com/aws-observability/aws-otel-community/tree/master/sample-apps/prometheus-sample-app)                 |Generates prometheus's metrics (counter, gauge, histogram,summary)                                                                             |Go        |  
|Javascript sample app        |[aws-otel-js](https://github.com/aws-observability/aws-otel-js/tree/main/sample-apps)                                                       |Continuous integration of ADOT components for X-Ray with Manual instrumentation of OpenTelemetry JavaScript SDK                                |JavaScript|
|Go Sample app                |[aws-otel-go](https://github.com/aws-observability/aws-otel-python/tree/main/integration-test-apps/none-instrumentation/flask)              |Complement the upstream OpenTelemetry Go with components for X-Ray                                                                             |Go        |
|.Net Sample app              |[aws-otel-dotnet](https://github.com/aws-observability/aws-otel-dotnet/tree/main/integration-test-app)                                      |Validates the continual integration with the AWS Distro for OpenTelemetry .NET and AWS X-Ray back-end service                                  |.Net      |
|Jmx                          |[aws-otel-test-framework](https://github.com/aws-observability/aws-otel-test-framework/tree/terraform/sample-apps/jmx)                      |Generates prometheus metrics                                                                                                                   |Java      |
|Jaeger-Zipkin                |[aws-otel-test-framework](https://github.com/aws-observability/aws-otel-test-framework/tree/terraform/sample-apps/jaeger-zipkin-sample-app) |Emits trace data using zipkin and jaeger                                                                                                       |Java      |
|Statsd                       |[aws-otel-testframework](https://github.com/aws-observability/aws-otel-test-framework/tree/terraform/sample-apps/statsd)                    |Emits metrics in statsd format                                                                                                                 |Python    |
|Prometheus sample app        |[aws-otel-test-framework](https://github.com/aws-observability/aws-otel-test-framework/tree/terraform/sample-apps/prometheus)               |Generates prometheus's metrics (counter, gauge, histogram,summary)                                                                             |Go        |


**Python instrumentation sample apps**

|Sample App                   |Location                                                                                                                                    |App functionality                                                                                                                              |
|-----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
|Python-auto instrumentation  |[aws-otel-python](https://github.com/aws-observability/aws-otel-python/tree/main/integration-test-apps/auto-instrumentation/flask)          |Continuous integration of ADOT components for X-Ray with Auto instrumentation of OpenTelemetry Python                                          |
|Python-manual instrumentation|[aws-otel-python](https://github.com/aws-observability/aws-otel-python/tree/main/integration-test-apps/manual-instrumentation/flask)        |Continuous integration of ADOT components for X-Ray with manual instrumentation of OpenTelemetry Python                                        |
|Python-none instrumentation  |[aws-otel-python](https://github.com/aws-observability/aws-otel-python/tree/main/integration-test-apps/none-instrumentation/flask)          |This application provides a baseline for performance testing, has no instrumentation, helps reveal the overhead that comes with instrumentation|


**Java instrumentation sample apps**

|Sample App                   |Location                                                                                                                                    |App functionality                                                                                                                              |
|-----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
|Spark-awssdk1                |[aws-otel-java-instrumentation](https://github.com/aws-observability/aws-otel-java-instrumentation/tree/main/sample-apps/spark-awssdkv1)    |Generates OTLP metrics and traces                                                                                                              |
|Spark                        |[aws-otel-java-instrumentation](https://github.com/aws-observability/aws-otel-java-instrumentation/tree/main/sample-apps/spark)             |Generates OTLP metrics and traces                                                                                                              |
|Springboot                   |[aws-otel-java-instrumentation](https://github.com/aws-observability/aws-otel-java-instrumentation/tree/main/sample-apps/springboot)        |Generates OTLP metrics and traces                                                                                                              |


**Ruby instrumentation sample app**

|Sample App                   |Location                                                                                                                                    |App functionality                                                                                                                              |
|-----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
|Ruby-manual-instrumentation  |[aws-otel-ruby](https://github.com/aws-observability/aws-otel-ruby/tree/main/sample-apps/manual-instrumentation/ruby-on-rails)              |Cotinuous integration of ADOT X-Ray components and X-Ray service. Manual Instrumentation using OpenTelemetry Ruby                              |

Sample App Spec: 

