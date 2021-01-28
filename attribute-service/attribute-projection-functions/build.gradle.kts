plugins {
  `java-library`
  jacoco
  id("org.hypertrace.jacoco-report-plugin")
  id("org.hypertrace.publish-plugin")
}

dependencies {
  api("com.google.code.findbugs:jsr305:3.0.2")
  implementation("com.github.f4b6a3:uuid-creator:2.7.7")

  testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
}

tasks.test {
  useJUnitPlatform()
}