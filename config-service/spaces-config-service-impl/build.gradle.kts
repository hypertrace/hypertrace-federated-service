plugins {
  `java-library`
  jacoco
  id("org.hypertrace.jacoco-report-plugin")
}

dependencies {
  api(project(":config-service:spaces-config-service-api"))
  implementation(project(":config-service:config-service-api"))
  implementation(project(":config-service:config-proto-converter"))
  implementation("com.google.guava:guava:30.1-jre")
  implementation("io.reactivex.rxjava3:rxjava:3.0.9")
  implementation("org.hypertrace.core.grpcutils:grpc-context-utils:0.3.3")
  implementation("org.hypertrace.core.grpcutils:grpc-client-utils:0.3.3")
  implementation("org.hypertrace.core.grpcutils:grpc-server-rx-utils:0.3.3")
  implementation("org.hypertrace.core.grpcutils:grpc-client-rx-utils:0.3.3")

  testImplementation("org.junit.jupiter:junit-jupiter:5.7.0")
  testImplementation("org.mockito:mockito-core:3.7.0")
  testImplementation("org.mockito:mockito-junit-jupiter:3.7.0")
  testImplementation(testFixtures(project(":config-service:config-service")))
}

tasks.test {
  useJUnitPlatform()
}