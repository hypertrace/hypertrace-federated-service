plugins {
  `java-library`
}


dependencies {
  api("io.grpc:grpc-protobuf:1.35.0")
  implementation("com.google.protobuf:protobuf-java-util:3.13.0")
  constraints {
    implementation("com.google.guava:guava:30.1-jre") {
      because("https://snyk.io/vuln/SNYK-JAVA-COMGOOGLEGUAVA-1015415")
    }
  }
}
