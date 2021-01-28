rootProject.name = "config-service"

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

include(":config-service-api")
include(":config-service-impl")
include(":config-service")

include(":config-proto-converter")

include(":spaces-config-service-api")
include(":spaces-config-service-impl")
