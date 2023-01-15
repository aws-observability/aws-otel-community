plugins {

    `java-library`
}
val otelVersion = "1.19.0"
val otelInstrumentationVersion = "1.19.2"

repositories {
    mavenCentral()
}
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    compileOnly("com.google.auto.service:auto-service:1.0.1")
    annotationProcessor("com.google.auto.service:auto-service:1.0.1")

    compileOnly("io.opentelemetry:opentelemetry-sdk-extension-autoconfigure-spi:${otelVersion}")
    compileOnly("io.opentelemetry.instrumentation:opentelemetry-instrumentation-api:${otelInstrumentationVersion}")
    compileOnly("io.opentelemetry.javaagent:opentelemetry-javaagent-extension-api:${otelInstrumentationVersion}-alpha")
    compileOnly("io.opentelemetry:opentelemetry-semconv:${otelVersion}-alpha")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
