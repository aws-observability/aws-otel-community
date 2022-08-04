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

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jk1.license.render.InventoryMarkdownReportRenderer
import nebula.plugin.release.git.opinion.Strategies
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    java

    id("com.diffplug.spotless")
    id("com.github.jk1.dependency-license-report")
    id("io.github.gradle-nexus.publish-plugin")
    id("nebula.release")

    id("com.github.johnrengelman.shadow") apply false
}

release {
    defaultVersionStrategy = Strategies.getSNAPSHOT()
}

nebulaRelease {
    addReleaseBranchPattern("""v\d+\.\d+\.x""")
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://aws.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://aws.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(System.getenv("PUBLISH_USERNAME"))
            password.set(System.getenv("PUBLISH_PASSWORD"))
        }
    }
}

val releaseTask = tasks.named("release")
val postReleaseTask = tasks.named("release")

allprojects {

    project.group = "software.amazon.opentelemetry"

    plugins.apply("com.diffplug.spotless")

    plugins.withType(BasePlugin::class) {
        val assemble = tasks.named("assemble")
        val check = tasks.named("check")

        releaseTask.configure {
            dependsOn(assemble, check)
        }
    }

    spotless {
        kotlinGradle {
            ktlint("0.40.0").userData(mapOf("indent_size" to "2", "continuation_indent_size" to "2"))

            // Doesn't support pluginManagement block
            targetExclude("settings.gradle.kts")

            if (!project.path.startsWith(":sample-apps:")) {
                licenseHeaderFile("${rootProject.projectDir}/config/license/header.java", "plugins|include|import")
            }
        }
    }

    plugins.withId("java") {
        java {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8

            withJavadocJar()
            withSourcesJar()
        }

        val dependencyManagement by configurations.creating {
            isCanBeConsumed = false
            isCanBeResolved = false
            isVisible = false
        }

        dependencies {
            dependencyManagement(platform(project(":dependencyManagement")))
            afterEvaluate {
                configurations.configureEach {
                    if (isCanBeResolved && !isCanBeConsumed) {
                        extendsFrom(dependencyManagement)
                    }
                }
            }

            testImplementation("org.assertj:assertj-core")
            testImplementation("org.junit.jupiter:junit-jupiter-api")
            testImplementation("org.junit.jupiter:junit-jupiter-params")
            testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
        }

        spotless {
            java {
                googleJavaFormat()

                if (!project.path.startsWith(":sample-apps:")) {
                    licenseHeaderFile("${rootProject.projectDir}/config/license/header.java")
                }
            }
        }

        val enableCoverage: String? by project
        if (enableCoverage == "true") {
            plugins.apply("jacoco")

            tasks {
                val build by named("build")
                withType<JacocoReport> {
                    build.dependsOn(this)

                    reports {
                        xml.isEnabled = true
                        html.isEnabled = true
                        csv.isEnabled = false
                    }
                }
            }
        }

        tasks {
            withType<Test> {
                useJUnitPlatform()

                testLogging {
                    exceptionFormat = TestExceptionFormat.FULL
                    showStackTraces = true
                }
            }

            named<JavaCompile>("compileTestJava") {
                sourceCompatibility = JavaVersion.VERSION_11.toString()
                targetCompatibility = JavaVersion.VERSION_11.toString()
            }
        }
    }

    plugins.withId("com.github.johnrengelman.shadow") {
        tasks {
            withType<ShadowJar>().configureEach {
                exclude("**/module-info.class")

                mergeServiceFiles()

                // rewrite library instrumentation dependencies
                relocate("io.opentelemetry.instrumentation", "io.opentelemetry.javaagent.shaded.instrumentation")

                // rewrite dependencies calling Logger.getLogger
                relocate("java.util.logging.Logger", "io.opentelemetry.javaagent.bootstrap.PatchLogger")

                // relocate OpenTelemetry API usage
                relocate("io.opentelemetry.api", "io.opentelemetry.javaagent.shaded.io.opentelemetry.api")
                relocate("io.opentelemetry.semconv", "io.opentelemetry.javaagent.shaded.io.opentelemetry.semconv")
                relocate("io.opentelemetry.spi", "io.opentelemetry.javaagent.shaded.io.opentelemetry.spi")
                relocate("io.opentelemetry.context", "io.opentelemetry.javaagent.shaded.io.opentelemetry.context")

                // relocate the OpenTelemetry extensions that are used by instrumentation modules)
                // these extensions live in the AgentClassLoader, and are injected into the user's class loader
                // by the instrumentation modules that use them
                relocate("io.opentelemetry.extension.aws", "io.opentelemetry.javaagent.shaded.io.opentelemetry.extension.aws")
                relocate("io.opentelemetry.extension.kotlin", "io.opentelemetry.javaagent.shaded.io.opentelemetry.extension.kotlin")
            }
        }
    }

    plugins.withId("maven-publish") {
        plugins.apply("signing")

        afterEvaluate {
            val publishTask = tasks.named("publishToSonatype")

            postReleaseTask.configure {
                dependsOn(publishTask)
            }
        }

        configure<PublishingExtension> {
            publications {
                register<MavenPublication>("maven") {
                    afterEvaluate {
                        artifactId = project.findProperty("archivesBaseName") as String
                    }

                    plugins.withId("java-platform") {
                        from(components["javaPlatform"])
                    }
                    plugins.withId("java") {
                        from(components["java"])
                    }

                    versionMapping {
                        allVariants {
                            fromResolutionResult()
                        }
                    }

                    pom {
                        name.set("AWS Distro for OpenTelemetry Java Agent")
                        description.set(
                            "The Amazon Web Services distribution of the OpenTelemetry Java Instrumentation."
                        )
                        url.set("https:/github.com/aws-observability/aws-otel-java-instrumentation")

                        licenses {
                            license {
                                name.set("Apache License, Version 2.0")
                                url.set("https://aws.amazon.com/apache2.0")
                                distribution.set("repo")
                            }
                        }

                        developers {
                            developer {
                                id.set("amazonwebservices")
                                organization.set("Amazon Web Services")
                                organizationUrl.set("https://aws.amazon.com")
                                roles.add("developer")
                            }
                        }

                        scm {
                            connection.set("scm:git:git@github.com:aws-observability/aws-otel-java-instrumentation.git")
                            developerConnection.set("scm:git:git@github.com:aws-observability/aws-otel-java-instrumentation.git")
                            url.set("https://github.com/aws-observability/aws-otel-java-instrumentation.git")
                        }
                    }
                }
            }
        }

        tasks.withType<Sign>().configureEach {
            onlyIf { System.getenv("CI") == "true" }
        }

        configure<SigningExtension> {
            val signingKey = System.getenv("GPG_PRIVATE_KEY")
            val signingPassword = System.getenv("GPG_PASSPHRASE")
            useInMemoryPgpKeys(signingKey, signingPassword)
            sign(the<PublishingExtension>().publications["maven"])
        }
    }
}

tasks {
    val cleanLicenseReport by registering(Delete::class) {
        delete("licenses")
    }

    named("generateLicenseReport") {
        dependsOn(cleanLicenseReport)
    }
}

licenseReport {
    renderers = arrayOf(InventoryMarkdownReportRenderer())
}

tasks {
    val cleanLicenses by registering(Delete::class) {
        delete("licenses")
    }

    val copyLicenses by registering(Copy::class) {
        dependsOn(cleanLicenses)

        from("build/reports/dependency-license")
        into("licenses")
    }

    val generateLicenseReport by existing {
        finalizedBy(copyLicenses)
    }
}

nebulaRelease {
    addReleaseBranchPattern("main")
}
