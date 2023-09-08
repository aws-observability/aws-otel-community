plugins {

    `java-library`
}

repositories {
    mavenCentral()
}
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    compileOnly("com.google.auto.service:auto-service:1.1.1")
    annotationProcessor("com.google.auto.service:auto-service:1.1.1")

    compileOnly("io.opentelemetry:opentelemetry-sdk-extension-autoconfigure-spi:1.30.0")
    compileOnly("io.opentelemetry.instrumentation:opentelemetry-instrumentation-api:1.29.0")
    compileOnly("io.opentelemetry.javaagent:opentelemetry-javaagent-extension-api:1.29.0-alpha")
    compileOnly("io.opentelemetry:opentelemetry-semconv:1.30.0-alpha")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
