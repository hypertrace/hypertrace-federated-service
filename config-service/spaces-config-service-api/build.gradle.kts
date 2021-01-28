import com.google.protobuf.gradle.*

plugins {
  `java-library`
  id("com.google.protobuf") version "0.8.14"
  id("org.hypertrace.publish-plugin")
}

protobuf {
  protoc {
    artifact = "com.google.protobuf:protoc:3.14.0"
  }
  plugins {
    id("grpc") {
      artifact = "io.grpc:protoc-gen-grpc-java:1.35.0"
    }
  }
  generateProtoTasks {
    ofSourceSet("main").forEach { task ->
      task.plugins {
        id("grpc")
      }
    }
  }
}

dependencies {
  api("io.grpc:grpc-protobuf:1.35.0")
  api("io.grpc:grpc-stub:1.35.0")
  api("javax.annotation:javax.annotation-api:1.3.2")
  constraints {
    implementation("com.google.guava:guava:30.1-jre") {
      because("https://snyk.io/vuln/SNYK-JAVA-COMGOOGLEGUAVA-1015415")
    }
  }
}

sourceSets {
  main {
    java {
      srcDirs("build/generated/source/proto/main/java", "build/generated/source/proto/main/grpc")
    }
  }
}