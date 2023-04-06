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
    id("com.google.cloud.tools.jib")
}

val otelVersion = "1.21.0"
val otelInstrumentationVersion = "1.21.0"
val otelContribVersion = "1.21.0"

repositories {
    mavenCentral()
}


repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

jib {
    from {
        image= "eclipse-temurin:17"
    }
    to {
        image = "java-manual-instrumentation-sample-app"
    }
    container {
        ports = listOf("8080")
    }
}

dependencies {

    // OpenTelemetry APIs and SDKs
    implementation(platform("io.opentelemetry:opentelemetry-bom:1.23.0"))
    implementation("io.opentelemetry:opentelemetry-api")
    implementation("io.opentelemetry:opentelemetry-sdk")

    // OpenTelemetry Exporters
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")

    // OpenTelemetry Aws Xray dependencies
    implementation("io.opentelemetry.contrib:opentelemetry-aws-xray-propagator:${otelContribVersion}-alpha")
    implementation("io.opentelemetry.contrib:opentelemetry-aws-xray:${otelContribVersion}")

    // OpenTelemetry AWS SDK Library Instrumentation
    implementation(platform("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom-alpha:${otelInstrumentationVersion}-alpha"))
    implementation("io.opentelemetry.instrumentation:opentelemetry-aws-sdk-2.2")

    // Opentelemetry OkHttp Library Instrumentation
    implementation("io.opentelemetry.instrumentation:opentelemetry-okhttp-3.0:${otelInstrumentationVersion}-alpha")

    implementation(project(":base"))

    constraints {
        implementation("com.fasterxml.jackson:jackson-bom:2.13.4.20221013") {
            because("bom used upstream is problematic. https://github.com/FasterXML/jackson-bom/issues/52#issuecomment-1292883281")
        }
    }

    implementation("org.apache.logging.log4j:log4j-api:2.18.0")
    implementation("org.apache.logging.log4j:log4j-core:2.18.0")
    implementation("org.slf4j:slf4j-simple:2.0.3")
}


application {
    // Define the main class for the application.
    mainClass.set("software.amazon.adot.sampleapp.MainManual")
}
