plugins {
  `java-library`
  jacoco
  id("org.hypertrace.jacoco-report-plugin")
}

tasks.test {
  useJUnitPlatform()
}

dependencies {
  constraints {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.0") {
      because("Deserialization of Untrusted Data [High Severity][https://snyk.io/vuln/SNYK-JAVA-COMFASTERXMLJACKSONCORE-561587] in com.fasterxml.jackson.core:jackson-databind@2.9.8\n" +
          "   used by org.apache.pinot:pinot-java-client")
    }
    implementation("io.netty:netty:3.10.6.Final") {
      because("HTTP Request Smuggling [Medium Severity][https://snyk.io/vuln/SNYK-JAVA-IONETTY-473694] in io.netty:netty@3.9.6.Final\n" +
          "    introduced by org.apache.pinot:pinot-java-client")
    }
    implementation("org.apache.zookeeper:zookeeper:3.6.2") {
      because("Authentication Bypass [High Severity][https://snyk.io/vuln/SNYK-JAVA-ORGAPACHEZOOKEEPER-32301] in org.apache.zookeeper:zookeeper@3.4.6\n" +
          "    introduced by org.apache.pinot:pinot-java-client")
    }
    implementation("commons-codec:commons-codec:1.13") {
      because("Information Exposure [Low Severity][https://snyk.io/vuln/SNYK-JAVA-COMMONSCODEC-561518] in commons-codec:commons-codec@1.11"
          + " introduced org.apache.httpcomponents:httpclient@4.5.12")
    }
  }
  api(project(":query-service:query-service-api"))
  api("com.typesafe:config:1.4.0")
  implementation("org.hypertrace.core.grpcutils:grpc-context-utils:0.3.0")
  implementation("org.hypertrace.core.grpcutils:grpc-client-utils:0.3.0")
  implementation("org.hypertrace.core.grpcutils:grpc-server-rx-utils:0.3.0")
  implementation("org.hypertrace.core.attribute.service:attribute-service-api:0.8.7")
  implementation("org.hypertrace.core.attribute.service:attribute-projection-registry:0.8.7")
  implementation("org.hypertrace.core.attribute.service:caching-attribute-service-client:0.8.7")
  implementation("org.hypertrace.core.serviceframework:service-framework-spi:0.1.18")
  implementation("com.google.inject:guice:4.2.3")
  implementation("org.apache.pinot:pinot-java-client:0.6.0") {
    // We want to use log4j2 impl so exclude the log4j binding of slf4j
    exclude("org.slf4j", "slf4j-log4j12")
  }
  implementation("org.slf4j:slf4j-api:1.7.30")
  implementation("commons-codec:commons-codec:1.13")
  implementation("org.hypertrace.core.serviceframework:platform-metrics:0.1.18")
  implementation("com.google.protobuf:protobuf-java-util:3.12.2")
  implementation("com.google.guava:guava:30.0-jre")
  implementation("io.reactivex.rxjava3:rxjava:3.0.6")

  testImplementation(project(":query-service:query-service-api"))
  testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
  testImplementation("org.mockito:mockito-core:3.3.3")
  testImplementation("org.mockito:mockito-junit-jupiter:3.3.3")
  testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:2.13.3")
}
