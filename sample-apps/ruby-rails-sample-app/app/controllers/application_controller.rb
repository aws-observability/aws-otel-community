
def get_xray_trace_id(otel_trace_id_hex)
    xray_trace_id = "1-#{otel_trace_id_hex[0..7]}-#{otel_trace_id_hex[8..otel_trace_id_hex.length]}"
    { traceId: xray_trace_id }
end

class ApplicationController < ActionController::Base
    def aws_sdk_call
        render json: "sdk"
    end
    
    def outgoing_http_call

        @@tracer.in_span("foo") do |span|
            Faraday.get('https://aws.amazon.com/')
        end

        render json: get_xray_trace_id(OpenTelemetry::Trace.current_span.context.hex_trace_id)
    end

    def outgoing_sampleapp
        render json: "outgoing sample app"
    end

    # Health check
    def root
        render "layouts/root"
    end
end