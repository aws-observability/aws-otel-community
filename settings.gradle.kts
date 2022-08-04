/*
 * Copyright Amazon.com, Inc. or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

pluginManagement {
    plugins {
        id("com.diffplug.spotless") version "6.7.2"
        id("com.github.ben-manes.versions") version "0.42.0"
        id("com.github.jk1.dependency-license-report") version "2.1"
        id("com.github.johnrengelman.shadow") version "7.1.2"
        id("com.google.cloud.tools.jib") version "3.2.1"
        id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
        id("nebula.release") version "16.0.0"
        id("org.springframework.boot") version "2.7.0"
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenLocal()

        maven {
            setUrl("https://oss.sonatype.org/content/repositories/snapshots")
        }
    }
}

include(":awsagentprovider")
include(":awspropagator")
include(":dependencyManagement")
include(":instrumentation:logback-1.0")
include(":instrumentation:log4j-2.13.2")
include(":otelagent")
include(":smoke-tests:fakebackend")
include(":smoke-tests:runner")
include(":smoke-tests:spring-boot")
include(":sample-apps:springboot")
include(":sample-apps:auto-instrumentation-java-spark")
include(":sample-apps:spark-awssdkv1")
