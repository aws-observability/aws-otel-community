# Parity Charts for AWS Distro for OpenTelemetry 

These charts outline the various language library components and features implemented in the AWS Distro for OpenTelemetry (ADOT) Collector to bridge the gap between OpenTelemetry and AWS X-Ray in terms of tracing capabilities.

|Features	|Java	|Javascript	|.NET	|Python	|Go	|
|---	|---	|---	|---	|---	|---	|
|AWS X-Ray Trace ID generation	|Done	|Done	|Done	|Done	|Done	|
|AWS X-Ray Trace ID propogation and trace header web framework handling	|Done	|Done	|Done	|Done	|Done	|
|Basic support for tracing call to AWS using language AWS SDK	|Done	|Done	|In progress	|Done	|In progress	|
|Library metadata	|Done	|Done	|Done	|Done	|Done	|
|DB/SQL support	|Done	|Done	|In review	|In review ([OTel Issue #159](https://github.com/open-telemetry/opentelemetry-python-contrib/issues/159))	|Done	|
|AWS X-Ray errors/exceptions format	|Done (via ADOT Collector)	|Done (via ADOT Collector)	|In OTel, need traslation done in collector	|Done (via ADOT Collector)	|Needs implementation in OTel ([OTel issue #1491](https://github.com/open-telemetry/opentelemetry-go/issues/1491))	|
|Resource Detectors - AWS Elastic Beanstalk	|Done	|Done	|EC2 (through ADOT Collector)	|EC2 (through ADOT Collector)	|Done	|
|Resource Detectors - AWS EC2	|Done	|Done	|EC2 (through ADOT Collector)	|EC2 (through ADOT Collector)	|Done	|
|Resource Detectors - AWS ECS	|Done	|Done	|Partial (through ADOT Collector) <br/> Container ID not recorded	|Partial (through ADOT Collector) <br/> Container ID not recorded	|Done	|
|Resource Detectors - AWS EKS	|Done	|Done	|Partial (through ADOT Collector)|Partial (Through ADOT Collector)|Done	|
|AWS X-Ray Trace ID injection into application logs	|Done	|	|	|	|	|
|Metadata/Annotations	|Done (through ADOT Collector)	|Done (through ADOT Collector)	|Done (through ADOT Collector)	|Done (through ADOT Collector)	|Done (Through Collector)	|
|AWS Lambda support	|	|	|	|Done (with autoinstumentation)	|	|
|Auto-instumentation	|Done	|Needs implementation in OTel	|Needs implementation in OTel	|Done	|Needs implementation in OTel	|
