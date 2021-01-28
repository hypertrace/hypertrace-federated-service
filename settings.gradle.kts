rootProject.name = "hypertrace-service"

pluginManagement {
  repositories {
    mavenLocal()
    gradlePluginPortal()
    maven("https://dl.bintray.com/hypertrace/maven")
  }
}

plugins {
  id("org.hypertrace.version-settings") version "0.1.2"
}

//includeBuild("./attribute-service")
//includeBuild("./entity-service")
//includeBuild("./gateway-service")
//includeBuild("./hypertrace-graphql")
includeBuild("./hypertrace-graphql/hypertrace-core-graphql")
//includeBuild("./config-bootstrapper")
//includeBuild("./config-service")

// attribute-service
include("attribute-service:attribute-projection-functions")
include("attribute-service:attribute-projection-registry")
include("attribute-service:attribute-service")
include("attribute-service:attribute-service-client")
include("attribute-service:attribute-service-api")
include("attribute-service:attribute-service-impl")
include("attribute-service:attribute-service-tenant-api")
include("attribute-service:caching-attribute-service-client")

// config-bootstrapper
include("config-bootstrapper:config-bootstrapper")

// config-service
include("config-service:config-proto-converter")
include("config-service:config-service")
include("config-service:config-service-api")
include("config-service:config-service-impl")
include("config-service:spaces-config-service-api")
include("config-service:spaces-config-service-impl")

// entity-service
include("entity-service:entity-data-service-rx-client")
include("entity-service:entity-service")
include("entity-service:entity-service-api")
include("entity-service:entity-service-client")
include("entity-service:entity-service-impl")
include("entity-service:entity-type-service-rx-client")

// gateway-service
include("gateway-service:gateway-service")
include("gateway-service:gateway-service-api")
include("gateway-service:gateway-service-impl")

// hypertrace-graphql-service
include("hypertrace-graphql:hypertrace-graphql-attribute-scope")
include("hypertrace-graphql:hypertrace-graphql-entity-schema")
include("hypertrace-graphql:hypertrace-graphql-entity-type")
include("hypertrace-graphql:hypertrace-graphql-explorer-context")
include("hypertrace-graphql:hypertrace-graphql-explorer-schema")
include("hypertrace-graphql:hypertrace-graphql-gateway-service-metric-utils")
include("hypertrace-graphql:hypertrace-graphql-impl")
include("hypertrace-graphql:hypertrace-graphql-metric-schema")
include("hypertrace-graphql:hypertrace-graphql-platform")
include("hypertrace-graphql:hypertrace-graphql-service")
include("hypertrace-graphql:hypertrace-graphql-service-config")
include("hypertrace-graphql:hypertrace-graphql-spaces-schema")

// query-service
include("query-service:query-service")
include("query-service:query-service-api")
include("query-service:query-service-client")
include("query-service:query-service-impl")


include(":hypertrace-service")
