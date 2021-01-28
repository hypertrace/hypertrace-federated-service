plugins {
  `java-library`
}

dependencies {
  api("com.google.inject:guice")
  api("io.reactivex.rxjava3:rxjava")
  implementation("org.hypertrace.core.graphql:hypertrace-core-graphql-attribute-store")
  implementation("org.hypertrace.core.graphql:hypertrace-core-graphql-common-schema")
  implementation(project(":hypertrace-graphql:hypertrace-graphql-explorer-schema"))

  compileOnly(project(":hypertrace-graphql:hypertrace-graphql-attribute-scope"))
}
