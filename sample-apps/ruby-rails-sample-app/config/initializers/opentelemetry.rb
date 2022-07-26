OpenTelemetry::SDK.configure do |c|
    c.service_name = 'aws-otel-manual-rails-sample'
  
    c.id_generator = OpenTelemetry::Propagator::XRay::IDGenerator
    c.propagators = [OpenTelemetry::Propagator::XRay::TextMapPropagator.new]
  

  
    # Alternatively, we could just enable all instrumentation:
    c.use_all({ 'OpenTelemetry::Instrumentation::ActiveRecord' => { enabled: false } })
  end
  