rootProject.name = "hypertrace-graphql"

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

includeBuild("./hypertrace-core-graphql")
include(":hypertrace-graphql-service")
include(":hypertrace-graphql-impl")
include(":hypertrace-graphql-gateway-service-metric-utils")
include(":hypertrace-graphql-metric-schema")
include(":hypertrace-graphql-entity-schema")
include(":hypertrace-graphql-explorer-schema")
include(":hypertrace-graphql-attribute-scope")
include(":hypertrace-graphql-explorer-context")
include(":hypertrace-graphql-entity-type")
include(":hypertrace-graphql-service-config")
include(":hypertrace-graphql-platform")
include(":hypertrace-graphql-spaces-schema")