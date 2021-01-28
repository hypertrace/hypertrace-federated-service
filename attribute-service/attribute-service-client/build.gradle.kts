plugins {
  `java-library`
  id("org.hypertrace.publish-plugin")
}

dependencies {
  api(project(":attribute-service:attribute-service-api"))
  api("com.typesafe:config:1.3.2")

  implementation("org.hypertrace.core.grpcutils:grpc-client-utils:0.2.0")
}
