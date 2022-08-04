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

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.jk1.license.LicenseReportExtension

plugins {
  `java-platform`

  id("com.github.ben-manes.versions")
}

data class DependencySet(val group: String, val version: String, val modules: List<String>)

val TEST_SNAPSHOTS = rootProject.findProperty("testUpstreamSnapshots") == "true" //

// This is the only version that affects the released artifact.
val otelVersion = "1.15.0" //
val otelSnapshotVersion = "1.16.0" //

// All versions below are only used in testing and do not affect the released artifact.

val DEPENDENCY_BOMS = listOf(
  "com.amazonaws:aws-java-sdk-bom:1.12.243",
  "com.fasterxml.jackson:jackson-bom:2.13.3",
  "com.google.guava:guava-bom:31.1-jre",
  "com.google.protobuf:protobuf-bom:3.21.1",
  "com.linecorp.armeria:armeria-bom:1.16.0",
  "io.grpc:grpc-bom:1.47.0",
  "io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom-alpha:${if (!TEST_SNAPSHOTS) "$otelVersion-alpha" else "$otelSnapshotVersion-alpha-SNAPSHOT"}",//
  "org.apache.logging.log4j:log4j-bom:2.17.2",
  "org.junit:junit-bom:5.8.2",
  "org.springframework.boot:spring-boot-dependencies:2.7.0", //might not need
  "org.testcontainers:testcontainers-bom:1.17.2",
  "software.amazon.awssdk:bom:2.17.214"
)

val DEPENDENCY_SETS = listOf(
  DependencySet(
    "org.assertj",
    "3.23.1",
    listOf("assertj-core")
  ),
  DependencySet(
    "org.curioswitch.curiostack",
    "2.0.0",
    listOf("protobuf-jackson")
  ),
  DependencySet(
    "org.slf4j",
    "1.7.36",
    listOf(
      "slf4j-api",
      "slf4j-simple"
    )
  )
)

val DEPENDENCIES = listOf(
  "commons-logging:commons-logging:1.2",
  "com.sparkjava:spark-core:2.9.3",
  "com.squareup.okhttp3:okhttp:4.10.0",
  "io.opentelemetry.contrib:opentelemetry-aws-xray:1.15.0",
  "io.opentelemetry.proto:opentelemetry-proto:0.17.0-alpha",
  "io.opentelemetry.javaagent:opentelemetry-javaagent:${if (!TEST_SNAPSHOTS) otelVersion else "$otelSnapshotVersion-SNAPSHOT"}", //might not need
  "net.bytebuddy:byte-buddy:1.12.10"
)

javaPlatform {
  allowDependencies()
}

dependencies {
  for (bom in DEPENDENCY_BOMS) {
    api(platform(bom))
  }
  constraints {
    for (set in DEPENDENCY_SETS) {
      for (module in set.modules) {
        api("${set.group}:$module:${set.version}")
      }
    }

    for (dependency in DEPENDENCIES) {
      api(dependency)
    }
  }
}

rootProject.allprojects {
  plugins.withId("com.github.jk1.dependency-license-report") {
    configure<LicenseReportExtension> {
      val bomExcludes = DEPENDENCY_BOMS.stream()
        .map { it.substring(0, it.lastIndexOf(':')) }
        .toArray { length -> arrayOfNulls<String>(length) }
      excludes = bomExcludes
    }
  }
}

fun isNonStable(version: String): Boolean { //might not need to 119
  val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
  val regex = "^[0-9,.v-]+(-r)?$".toRegex()
  val isGuava = version.endsWith("-jre")
  val isStable = stableKeyword || regex.matches(version) || isGuava
  return isStable.not()
}

tasks {
  named<DependencyUpdatesTask>("dependencyUpdates") {
    revision = "release"
    checkConstraints = true

    rejectVersionIf {
      isNonStable(candidate.version)
    }
  }
}
