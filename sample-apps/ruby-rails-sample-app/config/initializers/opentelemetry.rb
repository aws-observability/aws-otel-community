require 'opentelemetry/sdk'
require 'opentelemetry/exporter/otlp'
require 'opentelemetry/instrumentation/all'

OpenTelemetry::SDK.configure do |c|
    c.service_name = 'ruby-sample-app'
  
    c.id_generator = OpenTelemetry::Propagator::XRay::IDGenerator
    c.propagators = [OpenTelemetry::Propagator::XRay::TextMapPropagator.new]
  

  
    # Alternatively, we could just enable all instrumentation:
    c.use_all()
  end
  