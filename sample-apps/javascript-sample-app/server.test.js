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

const assert = require('assert').strict;
const http = require('http');
const axios = require('axios');
const { NodeTracerProvider } = require('@opentelemetry/sdk-trace-node');
const config = require('./config');

async function main() {
    const provider = new NodeTracerProvider();
    provider.register();
    const createServer = http.createServer;
    const createConfig = config.create_config;
    const get = axios.get;
    try {
        for (const ports of [[], [8081], [8081, 8082]]) {
            const requests = [];
            let handleRequest;
            config.create_config = () => ({ SampleAppPorts: ports });
            http.createServer = (handler) => {
                handleRequest = handler;
                return { listen: () => {} };
            };
            axios.get = (url) => {
                requests.push(url);
                return Promise.resolve({ status: 200 });
            };
            delete require.cache[require.resolve('./server')];
            require('./server').startServer();
            let response;
            await handleRequest({ url: '/outgoing-sampleapp' }, {
                end: (body) => { response = JSON.parse(body); },
                _contentLength: 0,
                statusCode: 200
            });
            assert.deepEqual(requests, ports.length
                ? ports.map((port) => `http://127.0.0.1:${port}/outgoing-sampleapp`)
                : ['https://aws.amazon.com']);
            assert.match(response.traceId, /^1-[a-f0-9]{8}-[a-f0-9]{24}$/);
        }
    } finally {
        http.createServer = createServer;
        config.create_config = createConfig;
        axios.get = get;
        await provider.shutdown();
    }
}

main().catch((error) => {
    console.error(error);
    process.exitCode = 1;
});
