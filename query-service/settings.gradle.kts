rootProject.name = "query-service"

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

include(":query-service-api")
include(":query-service-client")
include(":query-service-impl")
include(":query-service")
