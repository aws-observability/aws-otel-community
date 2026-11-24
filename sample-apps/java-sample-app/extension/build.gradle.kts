plugins {

    `java-library`
}

repositories {
    mavenCentral()
}
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:6.1.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:6.1.3")
    compileOnly("com.google.auto.service:auto-service:1.1.1")
    annotationProcessor("com.google.auto.service:auto-service:1.1.1")

    compileOnly("io.opentelemetry:opentelemetry-sdk-extension-autoconfigure-spi:1.68.0")
    compileOnly("io.opentelemetry.instrumentation:opentelemetry-instrumentation-api:2.34.0")
    compileOnly("io.opentelemetry.javaagent:opentelemetry-javaagent-extension-api:2.34.0-alpha")
    compileOnly("io.opentelemetry:opentelemetry-semconv:1.30.1-alpha")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
