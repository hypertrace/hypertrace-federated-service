subprojects {
  group = "org.hypertrace.config.service"
  pluginManager.withPlugin("org.hypertrace.publish-plugin") {
    configure<org.hypertrace.gradle.publishing.HypertracePublishExtension> {
      license.set(org.hypertrace.gradle.publishing.License.TRACEABLE_COMMUNITY)
    }
  }

  pluginManager.withPlugin("java") {
    configure<JavaPluginExtension> {
      sourceCompatibility = JavaVersion.VERSION_11
      targetCompatibility = JavaVersion.VERSION_11
    }
  }
}
