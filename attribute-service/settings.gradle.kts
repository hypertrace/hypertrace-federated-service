rootProject.name = "attribute-service"

pluginManagement {
  repositories {
    mavenLocal()
    gradlePluginPortal()
    maven("https://dl.bintray.com/hypertrace/maven")
  }
}

plugins {
  id("org.hypertrace.version-settings") version "0.1.1"
}

include(":attribute-service-api")
include(":attribute-service-client")
include(":attribute-service-impl")
include(":attribute-service")
include(":attribute-service-tenant-api")
include(":caching-attribute-service-client")
include(":attribute-projection-functions")
include(":attribute-projection-registry")

