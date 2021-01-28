plugins {
  `java-library`
}

dependencies {
  api("com.google.inject:guice")
  api("io.reactivex.rxjava3:rxjava")
  api("org.hypertrace.core.graphql:hypertrace-core-graphql-attribute-scope-constants")
  api("org.hypertrace.core.graphql:hypertrace-core-graphql-common-schema")
  implementation("org.hypertrace.core.graphql:hypertrace-core-graphql-attribute-store")
}
