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

// reads in config file
const yaml = require('js-yaml');
const fs = require('fs');

// Get cfg or throw error
function create_config(file) {
    try {
        const config = yaml.load(fs.readFileSync(file, 'utf8'));
        return config;
    }
    catch (e) {
    console.log(e);
    }
}

module.exports = {create_config};