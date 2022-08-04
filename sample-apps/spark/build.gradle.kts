plugins {
  java

  application
  id("com.google.cloud.tools.jib")
}

dependencies {
  implementation("commons-logging:commons-logging")
  implementation("com.sparkjava:spark-core")
  implementation("com.squareup.okhttp3:okhttp")
  implementation("io.opentelemetry:opentelemetry-api")
  implementation("org.apache.logging.log4j:log4j-core")
  implementation("software.amazon.awssdk:s3")
  implementation("software.amazon.awssdk:sts")
  implementation("org.yaml:snakeyaml:1.8")


  runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl")
}

application {
  mainClass.set("com.amazon.sampleapp.App")
}

jib {
  to {
    image = "public.ecr.aws/aws-otel-test/aws-otel-java-spark"
    tags = setOf("latest", "test-spark")
  }
  from {
    image = "public.ecr.aws/aws-otel-test/aws-opentelemetry-java-base:alpha"
//    platforms {
//      platform {
//        architecture = "amd64"
//        os = "linux"
//      }
//      platform {
//        architecture = "arm64"
//        os = "linux"
//      }
//    }
  }
}

tasks {
  named("jib") {
    dependsOn(":otelagent:jib")
  }
}
