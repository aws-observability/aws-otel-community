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
plugins {
    id("java-library")
}

repositories {
    mavenCentral()
}

dependencies {
    // Used to have access to the APIs
    api("io.opentelemetry:opentelemetry-api:1.47.0")

    // Third party libraries used in this application
    // Exposed to dependent modules
    api("com.sparkjava:spark-core:2.9.4")
    api("com.squareup.okhttp3:okhttp:4.12.0")
    api(platform("software.amazon.awssdk:bom:2.30.17"))
    api("software.amazon.awssdk:s3")

    // Not exposed to dependent modules
    implementation("org.yaml:snakeyaml:2.3")
    implementation("org.apache.logging.log4j:log4j-api:2.24.3")
    implementation("org.apache.logging.log4j:log4j-core:2.24.3")
    implementation("org.slf4j:slf4j-simple:2.0.16")

}

