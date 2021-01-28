plugins {
  `java-library`
  jacoco
  id("org.hypertrace.jacoco-report-plugin")
  id("org.hypertrace.publish-plugin")
}

dependencies {
  api(project(":attribute-service:attribute-service-api"))
  api("io.reactivex.rxjava3:rxjava:3.0.5")
  api("io.grpc:grpc-api:1.33.0")

  implementation("io.grpc:grpc-stub:1.33.0")
  implementation("org.hypertrace.core.grpcutils:grpc-client-utils:0.2.0")
  implementation("org.hypertrace.core.grpcutils:grpc-client-rx-utils:0.2.0")
  implementation("org.hypertrace.core.grpcutils:grpc-context-utils:0.2.0")
  implementation("com.google.guava:guava:30.0-jre")

  testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
  testImplementation("org.mockito:mockito-core:3.5.0")
  testImplementation("org.mockito:mockito-junit-jupiter:3.5.0")
  testImplementation("io.grpc:grpc-core:1.33.0")
  testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:2.13.3")
}

tasks.test {
  useJUnitPlatform()
}