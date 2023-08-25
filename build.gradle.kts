import org.jlleitschuh.gradle.ktlint.KtlintExtension

val javaVersion = JavaLanguageVersion.of(17)

group = "no.nav.tilleggsstonader.soknad"
version = "1.0.0"

plugins {
    application

    kotlin("jvm") version "1.9.10"
    id("org.jlleitschuh.gradle.ktlint") version "11.5.1"

    id("org.springframework.boot") version "3.1.2"
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("plugin.spring") version "1.9.0"

    id("org.cyclonedx.bom") version "1.7.4"
}

repositories {
    mavenCentral()
}

configure<KtlintExtension> {
    version.set("0.50.0")
}

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Logging
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")

    implementation("io.micrometer:micrometer-registry-prometheus")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

kotlin {
    jvmToolchain(javaVersion.asInt())

    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

application {
    mainClass.set("no.nav.tilleggsstonader.soknad.AppKt")
}

if (project.hasProperty("skipLint")) {
    gradle.startParameter.excludedTaskNames += "ktlintMainSourceSetCheck"
}

tasks.test {
    useJUnitPlatform()
}

tasks.bootJar {
    archiveFileName.set("app.jar")
}
