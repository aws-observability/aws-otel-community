require 'opentelemetry-api'
require 'opentelemetry-exporter-otlp'
require 'opentelemetry-sdk'
require 'opentelemetry-propagator-xray'

@@tracer = OpenTelemetry.tracer_provider.tracer("ADOT-Tracer-sample")

OpenTelemetry::SDK.configure do |c|
  c.service_name = "ruby-sample-app"
  c.use 'OpenTelemetry::Instrumentation::Rails'
  c.use 'OpenTelemetry::Instrumentation::Net::HTTP'
  c.use 'OpenTelemetry::Instrumentation::ConcurrentRuby'
  c.use 'OpenTelemetry::Instrumentation::Rack'
  c.use 'OpenTelemetry::Instrumentation::ActionPack'
  c.use 'OpenTelemetry::Instrumentation::ActiveSupport'
  c.use 'OpenTelemetry::Instrumentation::ActionView'
  c.use 'OpenTelemetry::Instrumentation::AwsSdk', {
    suppress_internal_instrumentation: true
  }

  c.id_generator = OpenTelemetry::Propagator::XRay::IDGenerator
  c.propagators = [OpenTelemetry::Propagator::XRay::TextMapPropagator.new]

end
