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
const meter = require('./meter');
const TOTAL_BYTES_SENT_METRIC = 'totalBytesSent';
const TOTAL_API_REQUESTS = 'apiRequests';
const LATENCY_TIME = 'latencyTime';
const attributes = { statusCode: '200',  metricType: 'random' };

let totalApiRequests = 0;
const totalBytesSentMetric = meter.createCounter(TOTAL_BYTES_SENT_METRIC, {
    description: "Keeps a sum of the total amount of bytes sent while the application is alive.",
    unit: 'mb'
});

const totalApiRequestsMetric = meter.createObservableCounter(TOTAL_API_REQUESTS, {
    description: "Increments by 1 every time a sample-app endpoint is used.",
    unit: '1'
});
totalApiRequestsMetric.addCallback((measurement) => {measurement.observe(totalApiRequests, attributes)});

const latencyTimeMetric = meter.createHistogram(LATENCY_TIME, {
    description: "Measures latency time.",
    unit: 'ms'
});

function updateTotalBytesSent(bytes, apiName, statusCode) {
    console.log("updating total bytes sent");
    const attributes = { 'apiName': apiName, 'statusCode': statusCode };
    totalBytesSentMetric.add(bytes, attributes);
};

function updateLatencyTime(returnTime, apiName, statusCode) {
    console.log("updating latency time");
    const attributes = { 'apiName': apiName, 'statusCode': statusCode };
    latencyTimeMetric.record(returnTime, attributes);
};

function updateApiRequestsMetric() {
    totalApiRequests += 1;
    console.log("API Requests:" + totalApiRequests);
}

module.exports = {updateLatencyTime, updateTotalBytesSent, updateApiRequestsMetric}