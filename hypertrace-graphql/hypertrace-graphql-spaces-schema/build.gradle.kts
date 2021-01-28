plugins {
  `java-library`
  jacoco
  id("org.hypertrace.jacoco-report-plugin")
}

dependencies {
  api("com.google.inject:guice")
  api("org.hypertrace.core.graphql:hypertrace-core-graphql-context")
  api("io.reactivex.rxjava3:rxjava")
  api("io.github.graphql-java:graphql-java-annotations")

  annotationProcessor("org.projectlombok:lombok")
  compileOnly("org.projectlombok:lombok")

  implementation("org.hypertrace.config.service:spaces-config-service-api")
  implementation(project(":hypertrace-graphql:hypertrace-graphql-service-config"))
  implementation("org.hypertrace.core.graphql:hypertrace-core-graphql-common-schema")
  implementation("org.hypertrace.core.graphql:hypertrace-core-graphql-grpc-utils")
  implementation("org.hypertrace.core.graphql:hypertrace-core-graphql-deserialization")
  implementation(project(":hypertrace-graphql:hypertrace-graphql-explorer-schema"))

  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("org.mockito:mockito-core")
  testImplementation("org.mockito:mockito-junit-jupiter")
}

tasks.test {
  useJUnitPlatform()
}
