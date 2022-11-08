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

const { SemanticAttributes } = require("@opentelemetry/semantic-conventions");
const metricsApi = require('@opentelemetry/api-metrics');

const TOTAL_BYTES_SENT_METRIC = 'totalBytesSent';
const TOTAL_API_REQUESTS = 'apiRequests';
const LATENCY_TIME = 'latencyTime';

let totalApiRequests = 0;

const commmon_attributes = { signal: 'metric',  language: 'javascript', metricType: 'request' };

const meter = metricsApi.metrics.getMeter('js-sample-app-meter');

const totalBytesSentMetric = meter.createCounter(TOTAL_BYTES_SENT_METRIC, {
    description: "Keeps a sum of the total amount of bytes sent while the application is alive.",
    unit: 'mb'
});

const totalApiRequestsMetric = meter.createObservableCounter(TOTAL_API_REQUESTS, {
    description: "Increments by 1 every time a sample-app endpoint is used.",
    unit: '1'
});
totalApiRequestsMetric.addCallback((measurement) => {measurement.observe(totalApiRequests, commmon_attributes)});

const latencyTimeMetric = meter.createHistogram(LATENCY_TIME, {
    description: "Measures latency time.",
    unit: 'ms'
});

function updateTotalBytesSent(bytes, apiName, statusCode) {
    console.log("Updating total bytes sent");
    const attributes = { signal: 'metric',  language: 'javascript', metricType: 'request', 'apiName': apiName, [SemanticAttributes.HTTP_STATUS_CODE]: statusCode };
    totalBytesSentMetric.add(bytes, attributes);
};

function updateLatencyTime(returnTime, apiName, statusCode) {
    console.log("Updating latency time");
    const attributes = { signal: 'metric',  language: 'javascript', metricType: 'request', 'apiName': apiName, [SemanticAttributes.HTTP_STATUS_CODE]: statusCode };
    latencyTimeMetric.record(returnTime, attributes);
};

function updateApiRequestsMetric() {
    totalApiRequests += 1;
    console.log("API Requests:" + totalApiRequests);
}

module.exports = {totalBytesSentMetric, totalApiRequestsMetric, latencyTimeMetric, updateLatencyTime, updateTotalBytesSent, updateApiRequestsMetric}
