plugins {
    id("java-library")
}

val otelVersion = "1.17.0"

repositories {
    mavenCentral()
}

dependencies {
    // Used to have access to the APIs
    api("io.opentelemetry:opentelemetry-api:${otelVersion}")

    // Third party libraries used in this application
    // Exposed to dependent modules
    api("com.sparkjava:spark-core:2.9.4")
    api("com.squareup.okhttp3:okhttp:4.10.0")
    api(platform("software.amazon.awssdk:bom:2.15.0"))
    api("software.amazon.awssdk:s3")

    // Not exposed to dependent modules
    implementation("org.yaml:snakeyaml:1.8")
    implementation("org.apache.logging.log4j:log4j-api:2.18.0")
    implementation("org.apache.logging.log4j:log4j-core:2.18.0")

}

