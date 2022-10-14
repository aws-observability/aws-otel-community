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
'use strict';

const { OTLPMetricExporter } = require('@opentelemetry/exporter-metrics-otlp-grpc');
const { metrics } = require('@opentelemetry/api-metrics');
const { MeterProvider, PeriodicExportingMetricReader } = require('@opentelemetry/sdk-metrics');
const { Resource } = require('@opentelemetry/resources');
const { SemanticResourceAttributes } = require('@opentelemetry/semantic-conventions')

/** The OTLP Metrics Provider with OTLP gRPC Metric Exporter and Metrics collection Interval  */

const meterProvider = new MeterProvider({
    resource: Resource.default().merge(new Resource({
        [SemanticResourceAttributes.SERVICE_NAME]: "js-sample-app",
        [SemanticResourceAttributes.PROCESS_PID]: process.pid,
        [SemanticResourceAttributes.DEPLOYMENT_ENVIRONMENT]: 'beta'
    })),
});

meterProvider.addMetricReader(new PeriodicExportingMetricReader({
    exporter: new OTLPMetricExporter(),
    exportIntervalMillis: 1000
}));

metrics.setGlobalMeterProvider(meterProvider);
module.exports = meterProvider.getMeter('js-sample-app-meter');
