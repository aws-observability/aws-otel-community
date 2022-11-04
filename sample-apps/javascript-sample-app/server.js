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

const sdk = require("./common");
const Worker = require("worker_threads");
const http = require("http");
const AWS = require('aws-sdk');
const api = require('@opentelemetry/api'); // get tracer

// config
const create_cfg = require('./config');
const cfg = create_cfg.create_config('./config.yaml');


// initialise sdk (metric and trace provider) and start server and a separate thread for random-metrics generation.
sdk.nodeSDKBuilder()
    .then(() => {
    startServer();
    const worker = new Worker.Worker('./random-metrics.js'); 
});

function startServer() {
    const server = http.createServer(handleRequest);
    server.listen(cfg.Port, cfg.Host, (err) => {
        if (err) {
            throw err;
        }
        console.log(`Node HTTP listening on ${cfg.Host}:${cfg.Port}`);
    });
}

function handleRequest(req, res) {
    const { updateTotalBytesSent, updateLatencyTime, updateApiRequestsMetric } = require('./request-metrics');
    const requestStartTime = new Date().getMilliseconds();
    const routeMapper = {
        '/': (req, res) => {
            res.end('OK.');
        },
        '/aws-sdk-call': (req, res) => {
            const s3 = new AWS.S3();
            s3.listBuckets(() => {
                console.log("Responding to /aws-sdk-call");
        
                res.end(getTraceIdJson());
                updateMetrics(res, '/aws-sdk-call', requestStartTime);
            });
        },
        '/outgoing-http-call': (req, res) => {
            http.get('http://aws.amazon.com', () => {
            console.log("Responding to /outgoing-http-call");
    
            res.end(getTraceIdJson());
            updateMetrics(res, '/aws-sdk-call', requestStartTime);
            });
        },
        '/outgoing-sampleapp': (req, res) => {
            if (cfg.SampleAppPorts.length > 0) {
                for (let i = 0; i < cfg.SampleAppPorts.length; i++) {
                    let port = cfg.SampleAppPorts[i];
                    if(!isNaN(port) && port > 0 && port <= 65535) {
                        let uri = `http://127.0.0.1:${port}/outgoing-sampleapp`;
                        http.get(uri, () => {
                            console.log(`made a request to ${uri}`);
                            updateMetrics(res, '/aws-sdk-call', requestStartTime);
                        });
                    } else {
                        console.log("SampleAppPorts is not a valid configuration!");
                    }
                }
            }
            else {
                http.get('http://aws.amazon.com', () => {
                    console.log('no ports configured. made a request to http://aws.amazon.com instead.');
                    updateMetrics(res, '/aws-sdk-call', requestStartTime);
                });
            }
            res.end(getTraceIdJson());
        }
    }
    try {
        const handler = routeMapper[req.url]
        if (handler) {
            handler (req, res);
        };
    } 
    catch (err) {
        console.log(err);
    }

    function updateMetrics(res, apiName, requestStartTime) {
        updateTotalBytesSent(res._contentLength + mimicPayLoadSize(), apiName, res.statusCode);
        updateLatencyTime(new Date().getMilliseconds() - requestStartTime, apiName, res.statusCode);
        updateApiRequestsMetric();
    }
    
}

function getTraceIdJson() {
    const otelTraceId = api.trace.getSpan(api.context.active()).spanContext().traceId;
    const timestamp = otelTraceId.substring(0, 8);
    const randomNumber = otelTraceId.substring(8);
    const xrayTraceId = "1-" + timestamp + "-" + randomNumber;
    return JSON.stringify({ "traceId": xrayTraceId });
  }

function mimicPayLoadSize() {
    return Math.random() * 1000;
}

