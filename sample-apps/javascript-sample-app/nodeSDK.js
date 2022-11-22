/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS'" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

'use strict'

const process = require('process');
const opentelemetry = require("@opentelemetry/sdk-node");

const { Resource } = require("@opentelemetry/resources");
const { SemanticResourceAttributes } = require("@opentelemetry/semantic-conventions");
const { PeriodicExportingMetricReader } = require("@opentelemetry/sdk-metrics");
const { OTLPMetricExporter } = require("@opentelemetry/exporter-metrics-otlp-grpc");
const { BatchSpanProcessor} = require('@opentelemetry/sdk-trace-base');
const { OTLPTraceExporter } = require('@opentelemetry/exporter-trace-otlp-grpc');
const { AWSXRayPropagator } = require("@opentelemetry/propagator-aws-xray");
const { AWSXRayIdGenerator } = require("@opentelemetry/id-generator-aws-xray");
const { HttpInstrumentation } = require("@opentelemetry/instrumentation-http");
const { AwsInstrumentation } = require("opentelemetry-instrumentation-aws-sdk");

const _resource = Resource.default().merge(new Resource({
        [SemanticResourceAttributes.SERVICE_NAME]: "js-sample-app",
    }));

const _traceExporter = new OTLPTraceExporter();
const _spanProcessor = new BatchSpanProcessor(_traceExporter);

const _tracerConfig = {
    idGenerator: new AWSXRayIdGenerator(),
}
const _metricReader = new PeriodicExportingMetricReader({
    exporter: new OTLPMetricExporter(),
    exportIntervalMillis: 1000
});

async function nodeSDKBuilder() {
    const sdk = new opentelemetry.NodeSDK({
        textMapPropagator: new AWSXRayPropagator(),
        metricReader: _metricReader,
        instrumentations: [
            new HttpInstrumentation(),
            new AwsInstrumentation({
                suppressInternalInstrumentation: true
            }),
        ],
        resource: _resource,
        spanProcessor: _spanProcessor,
        traceExporter: _traceExporter,
    });
    sdk.configureTracerProvider(_tracerConfig, _spanProcessor);
    
    // this enables the API to record telemetry
    await sdk.start(); 
    
    // gracefully shut down the SDK on process exit
    process.on('SIGTERM', () => {
    sdk.shutdown()
      .then(() => console.log('Tracing and Metrics terminated'))
      .catch((error) => console.log('Error terminating tracing and metrics', error))
      .finally(() => process.exit(0));
    });
}
module.exports = { nodeSDKBuilder };