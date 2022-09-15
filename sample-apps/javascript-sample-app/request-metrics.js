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

const TOTAL_BYTES_SENT_METRIC = 'totalBytesSent';
const TOTAL_API_REQUESTS = 'apiRequests';
const LATENCY_TIME = 'latencyTime';
const meter = require('./meter');

// variable to track number of api requests.
let n = 0;

const totalBytesSentMetric = meter.createCounter(TOTAL_BYTES_SENT_METRIC, {
    description: "Keeps a sum of the total amount of bytes sent while the application is alive.",
    unit: 'By'
});

// SumObserver is the same as ObservableCounter.
const totalApiRequestsMetric = meter.createSumObserver(TOTAL_API_REQUESTS, {
    description: "Increments by 1 every time a sampleapp endpoint is used.",
    unit: '1'
}, async (observerResult) => {
    const value = await getTotalApiRequests();
    observerResult.observe(value, { label: '1'}); 
});

// ValueRecorder is the same as histogram.
const latencyTimeMetric = meter.createValueRecorder(LATENCY_TIME, {
    description: "Measures latency time in buckets of 100, 300 and 500.",
    unit: 'ms'
});

function updateTotalBytesSent(bytes, apiName, statusCode) {
    console.log("updating total bytes sent");
    const labels = { 'apiName': apiName, 'statusCode': statusCode };
    totalBytesSentMetric.bind(labels).add(bytes);
};

function updateLatencyTime(returnTime, apiName, statusCode) {
    console.log("updating latency time");
    const labels = { 'apiName': apiName, 'statusCode': statusCode };
    latencyTimeMetric.bind(labels).record(returnTime);
};

function getTotalApiRequests() {
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve(n++);
        }, 100)
    })
}

module.exports = {updateLatencyTime, updateTotalBytesSent}