require "net/http"

def send_http_request(uri)
  uri = URI.parse(uri)
  http = Net::HTTP.new(uri.host, uri.port)
  http.use_ssl = false
  request = Net::HTTP::Get.new(uri.request_uri)
  res = http.request(request)
  return res
end

def send_https_request(uri)
  uri = URI.parse(uri)
  http = Net::HTTP.new(uri.host, uri.port)
  http.use_ssl = true
  request = Net::HTTP::Get.new(uri.request_uri)
  res = http.request(request)
  return res
end

##
# get_xray_trace_id returns a 'trace_id' consisting of 3 numbers seperated by hyphens.
# The first number is the version number
# The second number is 8 hexadecimal digits representing the time of the original request in Unix epoch time
# The third number is a 96-bit identifier for the trace, globally unique, 24 hexadecimal digit
# The conversion extracts the digits from the otel trace id and converts them into aws xray trace ID format

def get_xray_trace_id(otel_trace_id_hex)
  xray_trace_id = "1-#{otel_trace_id_hex[0..7]}-#{otel_trace_id_hex[8..otel_trace_id_hex.length]}"
  { traceId: xray_trace_id }
end

##
# This class is a controller class with 4 endpoints including a root endpoint

class ApplicationController < ActionController::Base
  ##
  # aws_sdk_call send a request to s3 to list the buckets of a current authenticated user.
  # Generates an Xray Trace ID.

  def aws_sdk_call
    @@tracer.in_span("get-aws-s3-buckets") do |span|
      s3 = Aws::S3::Client.new
      s3.list_buckets
    end
    render json: get_xray_trace_id(OpenTelemetry::Trace.current_span.context.hex_trace_id)
  end

  ##
  # outgoing_http_call makes an HTTP GET request to https://aws.amazon.com and generates an Xray Trace ID.

  def outgoing_http_call
    @@tracer.in_span("outgoing-http-call") do |span|
      res = send_https_request("https://aws.amazon.com/")

      span.set_attribute("signal", "trace")
      span.set_attribute("language", "ruby")
    end

    render json: get_xray_trace_id(OpenTelemetry::Trace.current_span.context.hex_trace_id)
  end

  ##
  # outgoing_sampleapp makes a request to another Sampleapp and generates an Xray Trace ID. It will instead make a request to amazon.com
  # if no ountgoing sampleapp ports are configured

  def outgoing_sampleapp
    @@tracer.in_span("invoke-sampleapps") do |span|
      count = $sample_app_ports.length()

      if count == 0
        # Make a leaf request
        @@tracer.in_span("leaf-request") do |span|
          res = send_https_request("https://aws.amazon.com/")

          span.set_attribute("signal", "trace")
          span.set_attribute("language", "ruby")
        end
      else
        # Call sample apps
        for port in $sample_app_ports
          @@tracer.in_span("invoke-sampleapp") do |span|
            uri = "http://" + port + "/outgoing-sampleapp"
            puts uri
            res = send_http_request("http://" + port + "/outgoing-sampleapp")

            span.set_attribute("signal", "trace")
            span.set_attribute("language", "ruby")
          end
        end
      end
    end

    render json: get_xray_trace_id(OpenTelemetry::Trace.current_span.context.hex_trace_id)
  end

  ##
  # Health check

  def root
    render json: "healthcheck"
  end
end
