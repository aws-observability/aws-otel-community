# Parity Charts for AWS Distro for OpenTelemetry 

These charts outline the various language library components and features implemented in the AWS Distro for OpenTelemetry (ADOT) Collector to bridge the gap between OpenTelemetry and AWS X-Ray in terms of tracing capabilities.

|Features	|Java	|Javascript	|.NET	|Python	|Go	|
|---	|---	|---	|---	|---	|---	|
|AWS X-Ray Trace ID generation	|Done	|Done	|Done	|Done	|Done	|
|AWS X-Ray Trace ID propogation and trace header web framework handling	|Done	|Done	|Done	|Done	|Done	|
|Basic support for tracing call to AWS using language AWS SDK	|Done	|Done	|Done	|Done	|Done	|
|Library metadata	|Done	|Done	|Done	|Done	|Done	|
|DB/SQL support	|Done	|Done	|In review	|In review ([OTel Issue #159](https://github.com/open-telemetry/opentelemetry-python-contrib/issues/159))	|Done	|
|AWS X-Ray errors/exceptions format	|Done (via ADOT Collector)	|Done (via ADOT Collector)	|Done (via ADOT Collector)	|Done (via ADOT Collector)	|Done (via ADOT Collector)	|
|Resource Detectors - AWS Elastic Beanstalk	|Done	|Done	|EC2 (through ADOT Collector)	|EC2 (through ADOT Collector)	|Done	|
|Resource Detectors - AWS EC2	|Done	|Done	|EC2 (through ADOT Collector)	|EC2 (through ADOT Collector)	|Done	|
|Resource Detectors - AWS ECS	|Done	|Done	|Done	|Done	|Done	|
|Resource Detectors - AWS EKS	|Done	|Done	|Done |Done |Done	|
|AWS X-Ray Trace ID injection into application logs	|Done	| NA	| NA	| NA	| NA   |
|Metadata/Annotations	|Done (through ADOT Collector)	|Done (through ADOT Collector)	|Done (through ADOT Collector)	|Done (through ADOT Collector)	|Done (Through ADOT Collector)	|
|AWS Lambda support	|Done	|Done	| NA |Done (with auto-instrumentation)	| NA	|
|Auto-instrumentation	|Done	|Needs implementation in OTel	|Needs implementation in OTel	|Done	|N/A	|
