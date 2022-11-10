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

const sdk = require("./nodeSDK");

// config
const create_cfg = require('./config');
const cfg = create_cfg.create_config('./config.yaml');

// validate SampleAppPorts provided in config 
validateSampleAppPorts();

// wait for initialization of nodesdk (metric and trace provider) and then start random and request based metric generation
sdk.nodeSDKBuilder()
    .then(() => {
        // request metrics generation
        const server = require('./server');
        server.startServer();

        // random metrics generation
        const { updateTimeAlive, updateThreadsActive, updateCpuUsageMetric, updateSizeMetric } = require('./random-metrics');
        setInterval(() => {
            updateTimeAlive();
            updateThreadsActive();
            updateCpuUsageMetric();
            updateSizeMetric();
        }, cfg.TimeInterval * 1000);
    });

function validateSampleAppPorts () {
    // validate SampleAppPorts provided in config 
    if (cfg.SampleAppPorts.length > 0) {
        for (let i = 0; i < cfg.SampleAppPorts.length; i++) {
            let port = cfg.SampleAppPorts[i];
            if(isNaN(port) || port < 0 || port > 65535) {
                throw new Error(`SampleAppPorts is not a valid configuration!`);
            }
        }
    }
}