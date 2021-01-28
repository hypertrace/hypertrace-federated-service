plugins {
  `java-library`
}

dependencies {
  api("com.google.inject:guice")
  api("com.graphql-java:graphql-java")
  api("org.hypertrace.core.graphql:hypertrace-core-graphql-attribute-store")
  api("org.hypertrace.core.graphql:hypertrace-core-graphql-context")
  api("io.reactivex.rxjava3:rxjava")
  api("io.github.graphql-java:graphql-java-annotations")

  annotationProcessor("org.projectlombok:lombok")
  compileOnly("org.projectlombok:lombok")

  implementation("org.hypertrace.core.graphql:hypertrace-core-graphql-deserialization")
  implementation("org.hypertrace.core.graphql:hypertrace-core-graphql-schema-utils")
  implementation("org.hypertrace.core.graphql:hypertrace-core-graphql-common-schema")
}

