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

// OTel JS - API
const { trace } = require('@opentelemetry/api');

// OTel JS - Core
const { NodeTracerProvider } = require('@opentelemetry/sdk-trace-node');
const { SimpleSpanProcessor, BatchSpanProcessor} = require('@opentelemetry/sdk-trace-base');

// OTel JS - Core - Exporters
const { OTLPTraceExporter } = require('@opentelemetry/exporter-trace-otlp-grpc');

// OTel JS - Core - Instrumentations
const { HttpInstrumentation } = require('@opentelemetry/instrumentation-http');
const { AwsInstrumentation } = require('opentelemetry-instrumentation-aws-sdk');
const { Resource } = require('@opentelemetry/resources');
const { SemanticResourceAttributes } = require('@opentelemetry/semantic-conventions')

// OTel JS - Contrib - AWS X-Ray
const { AWSXRayIdGenerator } = require('@opentelemetry/id-generator-aws-xray');
const { AWSXRayPropagator } = require('@opentelemetry/propagator-aws-xray');

const tracerProvider = new NodeTracerProvider({
  resource: Resource.default().merge(new Resource({
    [SemanticResourceAttributes.SERVICE_NAME]: "js-sample-app",
    [SemanticResourceAttributes.PROCESS_PID]: process.pid,
    [SemanticResourceAttributes.DEPLOYMENT_ENVIRONMENT]: 'beta'
  })),
  idGenerator: new AWSXRayIdGenerator(),
  instrumentations: [
    new HttpInstrumentation(),
    new AwsInstrumentation({
      suppressInternalInstrumentation: true
    }),
  ]
});

tracerProvider.addSpanProcessor(new BatchSpanProcessor(new OTLPTraceExporter()));

tracerProvider.register({
  propagator: new AWSXRayPropagator()
});

module.exports = trace.getTracer("js-sample-app-trace");
