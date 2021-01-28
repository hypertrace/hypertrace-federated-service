plugins {
  `java-library`
}

dependencies {
  api("com.google.inject:guice")
  api("io.reactivex.rxjava3:rxjava")
  implementation("org.hypertrace.core.graphql:hypertrace-core-graphql-attribute-store")
  implementation("org.hypertrace.core.graphql:hypertrace-core-graphql-common-schema")
  implementation("org.hypertrace.entity.service:entity-type-service-rx-client")
  implementation("org.hypertrace.core.graphql:hypertrace-core-graphql-grpc-utils")
  implementation(project(":hypertrace-graphql:hypertrace-graphql-entity-schema"))
  implementation(project(":hypertrace-graphql:hypertrace-graphql-service-config"))

  compileOnly(project(":hypertrace-graphql:hypertrace-graphql-attribute-scope"))
}
