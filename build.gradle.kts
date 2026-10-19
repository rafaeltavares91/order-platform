plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

description = "Production-oriented order processing platform"

allprojects {
    group = "dev.study"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-parameters", "-Xlint:all", "-Xlint:-processing"))
        options.release = 25
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            events("failed", "skipped")
        }
    }

    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(platform(libs.spring.cloud.aws.bom))
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.flyway)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.boot.starter.oauth2.resource.server)
    implementation(libs.spring.cloud.aws.starter.sns)
    implementation(libs.spring.cloud.aws.starter.sqs)
    implementation(libs.flyway.database.postgresql)
    implementation(libs.uuid.creator)
    runtimeOnly(libs.postgresql.driver)

    testImplementation(libs.spring.boot.test.actuator)
    testImplementation(libs.spring.boot.test.data.jpa)
    testImplementation(libs.spring.boot.test.flyway)
    testImplementation(libs.spring.boot.test.validation)
    testImplementation(libs.spring.boot.test.webmvc)
    testImplementation(libs.spring.boot.test.testcontainers)
    testImplementation(libs.spring.security.test)
    testImplementation(libs.testcontainers.localstack)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.archunit.junit5)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.register<Test>("unitTest") {
    group = "verification"
    description = "Runs unit and architecture tests without Testcontainers integration tests."
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    exclude("**/*IntegrationTest.class", "**/*IntegrationTest\$*.class")
    dependsOn(":domain:test")
}

tasks.named("check") {
    dependsOn(":domain:check")
}
