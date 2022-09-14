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

// setting up metrics
// const my_meter = require('./create-a-meter');
// const { emitsPayloadMetric, emitReturnTimeMetric } = require('./get-meter-emit-functions')(my_meter)

// TracerProvider must be initialized before instrumented packages
// (i.e. before 'aws-sdk' and 'http') are imported.

// setting up the traces and metrics
const tracer = require('./tracer');
const meter = require('./meter');

const http = require('http');
const AWS = require('aws-sdk');
const Worker = require('worker_threads');

const { updateTotalBytesSent, updateLatencyTime } = require('./request-metrics')(meter);

const api = require('@opentelemetry/api');
const create_cfg = require('./config');
const { DiagConsoleLogger } = require('@opentelemetry/api');

function startServer(file='./config.yaml') {
    const worker = new Worker.Worker('./random-metrics.js');
    const cfg = create_cfg.create_config(file);
    const server = http.createServer(handleRequest);
    server.listen(cfg.Port, cfg.Host, (err) => {
      if (err) {
        throw err;
      }
      console.log(`Node HTTP listening on ${cfg.Host}:${cfg.Port}`);
    });
}

function handleRequest(req, res) {
    const cfg = create_cfg.create_config('./config.yaml');
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
                updateTotalBytesSent(res._contentLength + mimicPayLoadSize(), '/aws-sdk-call', res.statusCode);
                updateLatencyTime(new Date().getMilliseconds() - requestStartTime, '/aws-sdk-call', res.statusCode);
            });
        },
        '/outgoing-http-call': (req, res) => {
            http.get('http://aws.amazon.com', () => {
            console.log("Responding to /outgoing-http-call");
    
            res.end(getTraceIdJson());
            updateTotalBytesSent(res._contentLength + mimicPayLoadSize(), '/outgoing-http-call', res.statusCode);
            updateLatencyTime(new Date().getMilliseconds() - requestStartTime, '/outgoing-http-call', res.statusCode);
            });
        },
        '/outgoing-sampleapp': (req, res) => {
            if (cfg.SampleAppPorts.length > 0) {
                for (let i = 0; i < cfg.SampleAppPorts.length; i++) {
                    let uri = `http://127.0.0.1:${cfg.SampleAppPorts[i]}/outgoing-sampleapp`;
                    http.get(uri, () => {
                        console.log(`made a request to ${uri}`);
                        updateTotalBytesSent(res._contentLength + mimicPayLoadSize(), '/outgoing-sampleapp', res.statusCode);
                        updateLatencyTime(new Date().getMilliseconds() - requestStartTime, '/outgoing-sampleapp', res.statusCode);
                    });
                }
            }
            else {
                http.get('http://aws.amazon.com', () => {
                    console.log('no ports configured. made a request to https://aws.amazon.com instead.');
                    updateTotalBytesSent(res._contentLength + mimicPayLoadSize(), '/outgoing-sampleapp', res.statusCode);
                    updateLatencyTime(new Date().getMilliseconds() - requestStartTime, '/outgoing-sampleapp', res.statusCode);
                });
            }
            res.end(getTraceIdJson());
        }
    }
    try {
        const handler = routeMapper[req.url]
        if (handler) {
            routeMapper[req.url] (req, res);
        };
    } 
    catch (err) {
        console.log(err);
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
  
startServer('./config.yaml')