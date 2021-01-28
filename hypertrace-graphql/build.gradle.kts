subprojects {
  group = "org.hypertrace.graphql"

  pluginManager.withPlugin("java") {
    configure<JavaPluginExtension> {
      sourceCompatibility = JavaVersion.VERSION_11
      targetCompatibility = JavaVersion.VERSION_11
    }
  }

  pluginManager.withPlugin("java-library") {
    dependencies {
      "api"(platform(project(":hypertrace-graphql:hypertrace-graphql-platform")))
      "annotationProcessor"(platform(project(":hypertrace-graphql:hypertrace-graphql-platform")))
      "testImplementation"(platform("org.hypertrace.core.graphql:hypertrace-core-graphql-test-platform"))
      "compileOnly"(platform(project(":hypertrace-graphql:hypertrace-graphql-platform")))
    }
  }
}
