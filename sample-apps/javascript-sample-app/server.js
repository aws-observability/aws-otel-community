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

const http = require('http');
const AWS = require('aws-sdk');
const fetch = require ("node-fetch");

// config
const create_cfg = require('./config');
const cfg = create_cfg.create_config('./config.yaml');

// tracer
const api = require('@opentelemetry/api'); 
const common_span_attributes = { signal: 'trace', language: 'javascript' };

// request metrics 
const { updateTotalBytesSent, updateLatencyTime, updateApiRequestsMetric } = require('./request-metrics');

// start server for request metrics and traces
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
    const requestStartTime = new Date().getMilliseconds();
    const routeMapper = {
        '/': (req, res) => {
            res.end('OK.');
        },
        '/aws-sdk-call': sdkCall,
        '/outgoing-http-call': outgoingHTTPCall,
        '/outgoing-sampleapp': outgoingSampleApp
    }
    try {
        const handler = routeMapper[req.url]
        if (handler) {
            handler (req, res);
            updateMetrics(res, req.url, requestStartTime);
        };
    } 
    catch (err) {
        console.log(err);
    }   
}

function sdkCall (req, res) {
    const s3 = new AWS.S3();
    s3.listBuckets(() => {
        console.log("Responding to /aws-sdk-call");
        res.end(getTraceIdJson());
    });
}

async function outgoingHTTPCall (req, res) {
    const traceid = await instrumentHTTPRequest('outgoing-http-call', 'https://aws.amazon.com');
    res.end(traceid);
}

async function outgoingSampleApp (req, res) {
    let traceid;
    if (cfg.SampleAppPorts.length > 0) {
    for (let i = 0; i < cfg.SampleAppPorts.length; i++) {
        let port = cfg.SampleAppPorts[i];
            let uri = `http://127.0.0.1:${port}/outgoing-sampleapp`;
            traceid = await instrumentHTTPRequest('/outgoing-sampleapp', uri);
        }
    }
    else {
        traceid = await instrumentHTTPRequest('/outgoing-sampleapp', 'https://aws.amazon.com');
    }
    res.end(traceid);
}

function updateMetrics (res, apiName, requestStartTime) {
    updateTotalBytesSent(res._contentLength + mimicPayLoadSize(), apiName, res.statusCode);
    updateLatencyTime(new Date().getMilliseconds() - requestStartTime, apiName, res.statusCode);
    updateApiRequestsMetric();
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

async function httpCall(url) {
    try {
        const response = await fetch(url); 
        console.log(`made a request to ${url}`);
        if (!response.ok) {
            throw new Error(`Error! status: ${response.status}`);
        }
    } catch (err) {
        throw new Error(`Error while fetching the ${url}`, err);
    }
}

async function instrumentHTTPRequest(spanName, url) {
    const tracer = api.trace.getTracer('js-sample-app-tracer');  
    const span = tracer.startSpan(spanName, {
        attributes: common_span_attributes
    });
    const ctx = api.trace.setSpan(api.context.active(), span);
    let traceid;
    await api.context.with(ctx, async () => {
        console.log(`Responding to ${spanName}`);
        await httpCall(url); 
        traceid = getTraceIdJson();
        span.end();
    });
    return traceid;
}

module.exports = {startServer};