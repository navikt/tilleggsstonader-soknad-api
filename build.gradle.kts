import org.jlleitschuh.gradle.ktlint.KtlintExtension

val javaVersion = JavaVersion.VERSION_17

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
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(kotlin("stdlib"))

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

java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

application {
    mainClass.set("no.nav.tilleggsstonader.soknad.AppKt")
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = javaVersion.toString()
            freeCompilerArgs += "-Xjsr305=strict"
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = javaVersion.toString()
            freeCompilerArgs += "-Xjsr305=strict"
            freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
        }
    }
    test {
        useJUnitPlatform()
    }
}

if (project.hasProperty("skipLint")) {
    gradle.startParameter.excludedTaskNames += "ktlintMainSourceSetCheck"
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    this.archiveFileName.set("app.jar")
}
