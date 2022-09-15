'use strict';

const { CollectorMetricExporter } = require('@opentelemetry/exporter-collector-grpc');
const { MeterProvider } = require('@opentelemetry/sdk-metrics-base');
const { Resource } = require('@opentelemetry/resources');
const { SemanticResourceAttributes } = require('@opentelemetry/semantic-conventions')

/** The OTLP Metrics Provider with OTLP gRPC Metric Exporter and Metrics collection Interval  */
module.exports = new MeterProvider({
    resource: Resource.default().merge(new Resource({
      [SemanticResourceAttributes.SERVICE_NAME]: "js-sampleapp"
    })),
    // Expects Collector at env variable `OTEL_EXPORTER_OTLP_ENDPOINT`, otherwise, http://localhost:4317
    exporter: new CollectorMetricExporter(),
    interval: 1000,
}).getMeter('js-sampleapp');