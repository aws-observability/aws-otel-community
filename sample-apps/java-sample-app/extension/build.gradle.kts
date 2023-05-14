plugins {

    `java-library`
}

repositories {
    mavenCentral()
}
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    compileOnly("com.google.auto.service:auto-service:1.0.1")
    annotationProcessor("com.google.auto.service:auto-service:1.0.1")

    compileOnly("io.opentelemetry:opentelemetry-sdk-extension-autoconfigure-spi:1.23.0")
    compileOnly("io.opentelemetry.instrumentation:opentelemetry-instrumentation-api:1.26.0")
    compileOnly("io.opentelemetry.javaagent:opentelemetry-javaagent-extension-api:1.23.0-alpha")
    compileOnly("io.opentelemetry:opentelemetry-semconv:1.23.0-alpha")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
