import com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer
import com.bmuschko.gradle.docker.tasks.container.DockerStartContainer
import com.bmuschko.gradle.docker.tasks.container.DockerStopContainer
import com.bmuschko.gradle.docker.tasks.image.DockerPullImage
import com.bmuschko.gradle.docker.tasks.network.DockerCreateNetwork

plugins {
  java
  application
  jacoco
  id("org.hypertrace.docker-java-application-plugin")
  id("org.hypertrace.docker-publish-plugin")
  id("org.hypertrace.integration-test-plugin")
  id("org.hypertrace.jacoco-report-plugin")
}

application {
  mainClassName = "org.hypertrace.core.bootstrapper.ConfigBootstrapper"
}

hypertraceDocker {
  defaultImage {
    javaApplication {
      serviceName.set("${project.name}")
    }
  }
}

tasks.register<DockerCreateNetwork>("createIntegrationTestNetwork") {
  networkName.set("config-bootstrapper-int-test")
  networkId.set("config-bootstrapper-int-test")
}

tasks.register<com.bmuschko.gradle.docker.tasks.network.DockerRemoveNetwork>("removeIntegrationTestNetwork") {
  networkId.set("config-bootstrapper-int-test")
}

tasks.register<DockerPullImage>("pullMongoImage") {
  image.set("mongo:4.2.6")
}

tasks.register<DockerCreateContainer>("createMongoContainer") {
  dependsOn("createIntegrationTestNetwork")
  dependsOn("pullMongoImage")
  targetImageId(tasks.getByName<DockerPullImage>("pullMongoImage").image)
  containerName.set("mongo-local")
  hostConfig.network.set(tasks.getByName<DockerCreateNetwork>("createIntegrationTestNetwork").networkId)
  hostConfig.portBindings.set(listOf("27017:27017"))
  hostConfig.autoRemove.set(true)
}

tasks.register<DockerStartContainer>("startMongoContainer") {
  dependsOn("createMongoContainer")
  targetContainerId(tasks.getByName<DockerCreateContainer>("createMongoContainer").containerId)
}

tasks.register<DockerStopContainer>("stopMongoContainer") {
  targetContainerId(tasks.getByName<DockerCreateContainer>("createMongoContainer").containerId)
  finalizedBy("removeIntegrationTestNetwork")
}

tasks.register<DockerPullImage>("pullAttributeServiceImage") {
  image.set("hypertrace/attribute-service:0.1.9")
}

tasks.register<DockerCreateContainer>("createAttributeServiceContainer") {
  dependsOn("pullAttributeServiceImage")
  targetImageId(tasks.getByName<DockerPullImage>("pullAttributeServiceImage").image)
  containerName.set("attribute-service-local")
  envVars.put("SERVICE_NAME", "attribute-service")
  envVars.put("mongo_host", tasks.getByName<DockerCreateContainer>("createMongoContainer").containerName)
  exposePorts("tcp", listOf(9012))
  hostConfig.network.set(tasks.getByName<DockerCreateNetwork>("createIntegrationTestNetwork").networkId)
  hostConfig.portBindings.set(listOf("9012:9012"))
  hostConfig.autoRemove.set(true)
}

tasks.register<DockerStartContainer>("startAttributeServiceContainer") {
  dependsOn("startMongoContainer")
  dependsOn("createAttributeServiceContainer")
  targetContainerId(tasks.getByName<DockerCreateContainer>("createAttributeServiceContainer").containerId)
}

tasks.register<DockerStopContainer>("stopAttributeServiceContainer") {
  targetContainerId(tasks.getByName<DockerCreateContainer>("createAttributeServiceContainer").containerId)
  finalizedBy("stopMongoContainer")
}

tasks.register<DockerPullImage>("pullEntityServiceImage") {
  image.set("hypertrace/entity-service:0.1.26")
}

tasks.register<DockerCreateContainer>("createEntityServiceContainer") {
  dependsOn("pullEntityServiceImage")
  targetImageId(tasks.getByName<DockerPullImage>("pullEntityServiceImage").image)
  containerName.set("entity-service-local")
  envVars.put("SERVICE_NAME", "entity-service")
  envVars.put("mongo_host", tasks.getByName<DockerCreateContainer>("createMongoContainer").containerName)
  envVars.put("BOOTSTRAP_CONFIG_URI", "file:///app/resources/configs")
  envVars.put("CLUSTER_NAME", "test")
  exposePorts("tcp", listOf(50061))
  hostConfig.portBindings.set(listOf("50061:50061"))
  hostConfig.binds.put("${projectDir}/src/integrationTest/resources/config-entity-service-test/application.conf", "/app/resources/configs/entity-service/test/application.conf")
  hostConfig.network.set(tasks.getByName<DockerCreateNetwork>("createIntegrationTestNetwork").networkId)
  hostConfig.autoRemove.set(true)
}

tasks.register<DockerStartContainer>("startEntityServiceContainer") {
  dependsOn("startMongoContainer")
  dependsOn("createEntityServiceContainer")
  targetContainerId(tasks.getByName<DockerCreateContainer>("createEntityServiceContainer").containerId)
}

tasks.register<DockerStopContainer>("stopEntityServiceContainer") {
  targetContainerId(tasks.getByName<DockerCreateContainer>("createEntityServiceContainer").containerId)
  finalizedBy("stopMongoContainer")
}

tasks.integrationTest {
  useJUnitPlatform()
  dependsOn("startAttributeServiceContainer")
  dependsOn("startEntityServiceContainer")
  finalizedBy("stopAttributeServiceContainer")
  finalizedBy("stopEntityServiceContainer")
}

tasks.test {
  useJUnitPlatform()
}

dependencies {
  implementation("org.hypertrace.entity.service:entity-service-client:0.4.1")
  implementation("org.hypertrace.entity.service:entity-service-api:0.4.1")
  implementation("org.hypertrace.core.documentstore:document-store:0.4.4")
  implementation("org.hypertrace.core.attribute.service:attribute-service-client:0.8.2")
  implementation("org.hypertrace.core.grpcutils:grpc-context-utils:0.3.1")
  implementation("org.hypertrace.core.grpcutils:grpc-client-utils:0.3.1")

  implementation("org.slf4j:slf4j-api:1.7.30")
  implementation("org.apache.logging.log4j:log4j-api:2.13.3")
  implementation("org.apache.logging.log4j:log4j-core:2.13.3")
  implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.13.3")
  implementation("org.apache.httpcomponents:httpclient:4.5.13")
  implementation("commons-io:commons-io:2.6")
  implementation("com.typesafe:config:1.4.0")
  implementation("com.google.protobuf:protobuf-java:3.13.0")
  implementation("com.google.protobuf:protobuf-java-util:3.13.0")
  implementation("commons-cli:commons-cli:1.4")
  implementation("org.reflections:reflections:0.9.12")
  implementation("com.fasterxml.jackson.core:jackson-core:2.11.1")
  implementation("com.fasterxml.jackson.core:jackson-databind:2.11.1")

  runtimeOnly("io.grpc:grpc-netty:1.33.0")

  constraints {
    implementation("com.google.guava:guava:30.0-jre") {
      because("Information Disclosure [Medium Severity][https://snyk.io/vuln/SNYK-JAVA-COMGOOGLEGUAVA-1015415] in com.google.guava:guava@29.0-android")
    }
    implementation("commons-codec:commons-codec:1.15") {
      because("https://snyk.io/vuln/SNYK-JAVA-COMMONSCODEC-561518")
    }
    runtimeOnly("io.netty:netty-handler-proxy:4.1.53.Final") {
      because("https://snyk.io/vuln/SNYK-JAVA-IONETTY-1020439")
    }
  }

  testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
  testImplementation("org.mockito:mockito-core:3.3.3")

  integrationTestImplementation("org.hypertrace.core.serviceframework:integrationtest-service-framework:0.1.19")
  integrationTestImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
}
