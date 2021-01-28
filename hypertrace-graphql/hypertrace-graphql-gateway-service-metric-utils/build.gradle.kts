plugins {
  `java-library`
}

dependencies {
  api("com.google.inject:guice")
  api("org.hypertrace.gateway.service:gateway-service-api")
  api("org.hypertrace.core.graphql:hypertrace-core-graphql-attribute-store")
  api("io.reactivex.rxjava3:rxjava")
  api("org.hypertrace.core.graphql:hypertrace-core-graphql-common-schema")
  api(project(":hypertrace-graphql:hypertrace-graphql-metric-schema"))
  api("org.hypertrace.core.graphql:hypertrace-core-graphql-gateway-service-utils")

  annotationProcessor("org.projectlombok:lombok")
  compileOnly("org.projectlombok:lombok")
}
