from config import *

import random_metrics
import random
import requests
import boto3

from opentelemetry.propagators.aws.aws_xray_propagator import (
    TRACE_ID_DELIMITER,
    TRACE_ID_FIRST_PART_LENGTH,
    TRACE_ID_VERSION,
)

from opentelemetry import trace, metrics

from flask import Flask, request


from opentelemetry.metrics import CallbackOptions, Observation


tracer = trace.get_tracer(__name__)
meter = metrics.get_meter(__name__)

app = Flask(__name__)

cfg = create_config('config.yaml')

"""
TRACES & REQUEST BASED METRICS
"""
# variable to keep track of the total number api requests
n = 0

# update_total_bytes_sent updates the metric with a value between 0 and 1024
def update_total_bytes_sent():
    min = 0 
    max = 1024
    total_bytes_sent.add(random.randint(min,max))

# update latency time updates the metric with a value between 0 and 512
def update_latency_time():
    min = 0
    max = 512
    latency_time.record(random.randint(min, max))

# Starts the callback for totalApiRequests
def api_requests_callback(options: CallbackOptions):
    global n
    n += 1
    add_api_request = Observation(value=n)
    print("api_requests called by SDK")
    yield add_api_request

# Converts otel trace id's to an xray format
def convert_otel_trace_id_to_xray(otel_trace_id_decimal):
    otel_trace_id_hex = "{:032x}".format(otel_trace_id_decimal)
    x_ray_trace_id = TRACE_ID_DELIMITER.join(
        [
            TRACE_ID_VERSION,
            otel_trace_id_hex[:TRACE_ID_FIRST_PART_LENGTH],
            otel_trace_id_hex[TRACE_ID_FIRST_PART_LENGTH:],
        ]
    )
    return '{{"traceId": "{}"}}'.format(x_ray_trace_id)

# register total bytes sent counter
total_bytes_sent=meter.create_counter(
    name="totalBytesSent",
    description="Keeps a sum of the total amount of bytes sent while application is alive",
    unit='By'
)

# register api requests observable counter
total_api_requests=meter.create_observable_counter(
    name="apiRequests",
    callbacks=[api_requests_callback],
    description="Increments by one every time a sampleapp endpoint is used",
    unit='1'
)

# registers latency time histogram
latency_time=meter.create_histogram(
    name="latencyTime",
    description="Measures latency time in buckets of 100, 300 and 500",
    unit='ms'
        )

# Test HTTP instrumentation
@app.route("/outgoing-http-call")
def call_http():
    requests.get("https://aws.amazon.com/")

    
    update_total_bytes_sent()
    update_latency_time()
    print("updating bytes sent & latency time...")

    return app.make_response(
        convert_otel_trace_id_to_xray(
            trace.get_current_span().get_span_context().trace_id
        )
    )
    

# Test AWS SDK instrumentation
@app.route("/aws-sdk-call")
def call_aws_sdk():

    update_total_bytes_sent()
    update_latency_time()

    client = boto3.client("s3")
    client.list_buckets()

    return app.make_response(
        convert_otel_trace_id_to_xray(
            trace.get_current_span().get_span_context().trace_id
        )
    )

# when this sample-app is invoked either by itself or a different sample app
@app.route("/outgoing-sampleapp")
def invoke():
    # Call sample apps
    ports = cfg.get("SampleAppPorts")
    if ports:
        for port in ports:
            uri = f"http://127.0.0.1:{port}/outgoing-sampleapp"
            print("making a request to: " + uri)
            r = requests.get(uri)

    # If no sample apps are defined in the config file the app makes a request to amazon.
    else:
        print("no ports configured. making a request to https://aws.amazon.com instead.")
        requests.get("https://aws.amazon.com/")
    
    update_total_bytes_sent()
    update_latency_time()
    
    return app.make_response(
        convert_otel_trace_id_to_xray(
            trace.get_current_span().get_span_context().trace_id
        )
    )
    

# Test Root Endpoint
@app.route("/")
def root_endpoint():
    return "<h1>App running!</h1>"

if __name__ == '__main__':
    # starting the random metric collector
    rmc = random_metrics.RandomMetricCollector()
    rmc.register_metrics_client(cfg)
    app.run(host=cfg['Host'], port=cfg['Port'])

