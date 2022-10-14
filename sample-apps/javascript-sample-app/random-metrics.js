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
create_cfg = require('./config');

const cfg = create_cfg.create_config('./config.yaml');
const TIME_ALIVE_METRIC = 'timeAlive';
const CPU_USAGE_METRIC = 'cpuUsage';
const THREADS_ACTIVE_METRIC = 'threadsActive';
const HEAP_SIZE_METRIC = 'totalHeapSize';

const attributes = { statusCode: '200',  metricType: 'random' };

let threadBool = true;
let threadCount = 0;
let cpuUsage = 0;
let totalHeapSize = 0;


const timeAliveMetric = meter.createCounter(TIME_ALIVE_METRIC, {
    description: 'Total amount of time that the application has been alive',
    unit: 's'
});

const threadsActiveMetric = meter.createUpDownCounter(THREADS_ACTIVE_METRIC, {
    description: 'The total number of threads active',
    unit:'1'
});

const cpuUsageMetric = meter.createObservableGauge(CPU_USAGE_METRIC, {
    description: 'Cpu usage percent',
    unit: '1'
});
cpuUsageMetric.addCallback((measurement) => {measurement.observe(cpuUsage, attributes)});

const totalHeapSizeMetric = meter.createObservableUpDownCounter(HEAP_SIZE_METRIC, {
    description: 'The current total heap size',
    unit:'1'
});
totalHeapSizeMetric.addCallback((measurement) => {measurement.observe(totalHeapSize, attributes)});

function updateCpuUsageMetric() {
    cpuUsage = Math.random() * cfg.RandomCpuUsageUpperBound;
}

function updateSizeMetric() {
    totalHeapSize += Math.random() * cfg.RandomTotalHeapSizeUpperBound;
}

function updateTimeAlive() {
    timeAliveMetric.add(cfg.RandomTimeAliveIncrementer, attributes);
}

function updateThreadsActive() {
    if (threadBool) {
        if (threadCount < cfg.RandomThreadsActiveUpperBound) {
            threadsActiveMetric.add(1, attributes);
            threadCount++;
        }
        else {
            threadBool = false;
            threadCount--;
        }
    }
    else {
        if (threadCount > 0) {
            threadsActiveMetric.add(-1, attributes);
            threadCount--;
        }
        else {
            threadBool = true;
            threadCount++;
        }
    }

}

setInterval(() => {
    updateTimeAlive();
    updateThreadsActive();
    updateCpuUsageMetric();
    updateSizeMetric();
}, cfg.TimeInterval * 1000);
