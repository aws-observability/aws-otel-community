require 'net/http'

def get_xray_trace_id(otel_trace_id_hex)
    xray_trace_id = "1-#{otel_trace_id_hex[0..7]}-#{otel_trace_id_hex[8..otel_trace_id_hex.length]}"
    { traceId: xray_trace_id }
end

class ApplicationController < ActionController::Base
    def aws_sdk_call
        @@tracer.in_span("get-aws-s3-buckets") do |span|
            s3 = Aws::S3::Client.new
            s3.list_buckets
        end
        render json: get_xray_trace_id(OpenTelemetry::Trace.current_span.context.hex_trace_id)
    end
    
    def outgoing_http_call

        @@tracer.in_span("outgoing-http-call") do |span|
            uri = URI('https://aws.amazon.com')
            Net::HTTP.get(uri) 
        end

        render json: get_xray_trace_id(OpenTelemetry::Trace.current_span.context.hex_trace_id)
    end

    def outgoing_sampleapp

        @@tracer.in_span("invoke-sample-apps") do |span|
            count = $sample_app_ports.length()

            if count == 0 
                # Make a leaf request
                @@tracer.in_span("leaf-request") do |span|
            
                    uri = URI('https://amazon.com')
                    Net::HTTP.get(uri) 
                end
            else
                # Call sample apps
                for port in $sample_app_ports do
                    @@tracer.in_span("invoke-sampleapp") do |span|
                        uri = URI("http://0.0.0.0:"+ port + "/outgoing-sampleapp")
                        Net::HTTP.get(uri)
                    end
                end     
            end
        end
        
        render json: get_xray_trace_id(OpenTelemetry::Trace.current_span.context.hex_trace_id)

    end

    # Health check
    def root
        render json: "healthcheck"
    end
end