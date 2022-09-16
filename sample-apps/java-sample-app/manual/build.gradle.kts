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
    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

val otelVersion = "1.17.0"

repositories {
    mavenCentral()
}


repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {

    // OpenTelemetry APIs and SDKs
    implementation(platform("io.opentelemetry:opentelemetry-bom:${otelVersion}"))
    implementation("io.opentelemetry:opentelemetry-api")
    implementation("io.opentelemetry:opentelemetry-sdk")

    // OpenTelemetry Exporters
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")

    // OpenTelemetry Aws Xray dependencies
    implementation("io.opentelemetry:opentelemetry-extension-aws")
    implementation("io.opentelemetry:opentelemetry-sdk-extension-aws")
    implementation("io.opentelemetry.contrib:opentelemetry-aws-xray:${otelVersion}")

    // OpenTelemetry AWS SDK Library Instrumentation
    implementation(platform("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom-alpha:${otelVersion}-alpha"))
    implementation("io.opentelemetry.instrumentation:opentelemetry-aws-sdk-2.2")

    // Opentelemetry OkHttp Library Instrumentation
    implementation("io.opentelemetry.instrumentation:opentelemetry-okhttp-3.0:${otelVersion}-alpha")

    implementation(project(":base"))
}


application {
    // Define the main class for the application.
    mainClass.set("software.amazon.adot.sampleapp.MainManual")
}
