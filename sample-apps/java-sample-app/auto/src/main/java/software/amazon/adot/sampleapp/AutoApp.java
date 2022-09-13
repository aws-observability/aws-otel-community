/*
 * Copyright The OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package software.amazon.adot.sampleapp;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Sample App class used for the case the Auto Instrumentation with the Java Agent is used.
 */
public class AutoApp extends BaseApp {

    public AutoApp(Config config) {
        super(config);
    }

    // Customizations for Auto instrumentation using the Agent. We can see that we are using the defaults because the
    // Java Agent is responsible for automatically instrument the third party code.
    @Override
    protected Call.Factory buildHttpClient() {
        return new OkHttpClient.Builder().build();
    }

    @Override
    protected S3Client buildS3Client() {
        return S3Client.builder()
                .build();
    }
}
