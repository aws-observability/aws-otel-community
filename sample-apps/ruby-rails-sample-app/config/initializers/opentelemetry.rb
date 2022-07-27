require 'opentelemetry/sdk'
require 'opentelemetry/exporter/otlp'
require 'opentelemetry/instrumentation/all'

OpenTelemetry::SDK.configure do |c|
    c.service_name = 'ruby-sample-app'
  
    c.id_generator = OpenTelemetry::Propagator::XRay::IDGenerator
    c.propagators = [OpenTelemetry::Propagator::XRay::TextMapPropagator.new]
  
    c.add_span_processor(
      OpenTelemetry::SDK::Trace::Export::BatchSpanProcessor.new(
        OpenTelemetry::Exporter::OTLP::Exporter.new(
          endpoint: 'http://0.0.0.0:4318/v1/traces'
        )
      )
    )
  
    # Alternatively, we could just enable all instrumentation:
    c.use_all()
  end
  
  @@tracer = OpenTelemetry.tracer_provider.tracer('my_app_or_gem', '0.1.0')
