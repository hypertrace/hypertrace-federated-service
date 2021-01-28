plugins {
  `java-library`
  jacoco
  id("org.hypertrace.jacoco-report-plugin")
}

dependencies {
  api("org.hypertrace.core.graphql:hypertrace-core-graphql-spi")
  api("com.graphql-java-kickstart:graphql-java-servlet")
  api(project(":hypertrace-graphql:hypertrace-graphql-service-config"))

  implementation("org.hypertrace.core.graphql:hypertrace-core-graphql-schema-registry")
  implementation("org.hypertrace.core.graphql:hypertrace-core-graphql-context")
  implementation("org.hypertrace.core.graphql:hypertrace-core-graphql-deserialization")
  implementation("org.hypertrace.core.graphql:hypertrace-core-graphql-grpc-utils")
  implementation("org.hypertrace.core.graphql:hypertrace-core-graphql-schema-utils")
  implementation("org.hypertrace.core.graphql:hypertrace-core-graphql-gateway-service-utils")
  implementation("org.hypertrace.core.graphql:hypertrace-core-graphql-rx-utils")
  implementation(project(":hypertrace-graphql:hypertrace-graphql-attribute-scope"))
  implementation(project(":hypertrace-graphql:hypertrace-graphql-gateway-service-metric-utils"))
  implementation("org.hypertrace.core.graphql:hypertrace-core-graphql-attribute-store")
  implementation("org.hypertrace.core.graphql:hypertrace-core-graphql-common-schema")
  implementation(project(":hypertrace-graphql:hypertrace-graphql-metric-schema"))
  implementation("org.hypertrace.core.graphql:hypertrace-core-graphql-metadata-schema")
  implementation("org.hypertrace.core.graphql:hypertrace-core-graphql-span-schema")
  implementation("org.hypertrace.core.graphql:hypertrace-core-graphql-trace-schema")
  implementation(project(":hypertrace-graphql:hypertrace-graphql-entity-schema"))
  implementation(project(":hypertrace-graphql:hypertrace-graphql-explorer-schema"))
  implementation(project(":hypertrace-graphql:hypertrace-graphql-explorer-context"))
  implementation(project(":hypertrace-graphql:hypertrace-graphql-entity-type"))
  implementation(project(":hypertrace-graphql:hypertrace-graphql-spaces-schema"))

  implementation("org.slf4j:slf4j-api")
  implementation("com.google.inject:guice")
  runtimeOnly("io.grpc:grpc-netty")

  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("org.mockito:mockito-core")
  testImplementation("org.mockito:mockito-junit-jupiter")
}

tasks.test {
  useJUnitPlatform()
}
