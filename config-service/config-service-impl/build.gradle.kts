plugins {
  `java-library`
  jacoco
  id("org.hypertrace.jacoco-report-plugin")
}

dependencies {
  api(project(":config-service:config-service-api"))

  implementation("com.fasterxml.jackson.core:jackson-databind:2.10.5.1")
  implementation("com.google.guava:guava:30.1-jre")
  implementation("com.google.protobuf:protobuf-java-util:3.13.0")
  implementation("com.typesafe:config:1.4.0")
  implementation("org.slf4j:slf4j-api:1.7.30")

  implementation("org.hypertrace.core.documentstore:document-store:0.4.5")
  implementation("org.hypertrace.core.grpcutils:grpc-context-utils:0.3.3")

  annotationProcessor("org.projectlombok:lombok:1.18.12")
  compileOnly("org.projectlombok:lombok:1.18.12")

  testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
  testImplementation("org.mockito:mockito-core:3.3.3")
  testImplementation("org.hypertrace.core.grpcutils:grpc-client-utils:0.3.3")
}

tasks.test {
  useJUnitPlatform()
}
