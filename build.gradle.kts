val javaVersion = JavaLanguageVersion.of(21)
val tilleggsstønaderLibsVersion = "2025.09.11-09.26.d3123ecc47ce"
val tilleggsstønaderKontrakterVersion = "2025.10.08-15.10.56740e4133f8"
val familieProsesseringVersion = "2.20250922094930_4bb329c"
val tokenSupportVersion = "5.0.37"
val wiremockVersion = "3.0.1"
val testcontainerVersion = "1.21.3"

group = "no.nav.tilleggsstonader.soknad"
version = "1.0.0"

plugins {
    application

    kotlin("jvm") version "2.2.20"
    id("com.diffplug.spotless") version "8.0.0"
    id("com.github.ben-manes.versions") version "0.53.0"
    id("se.patrikerdes.use-latest-versions") version "0.2.19"

    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.spring") version "2.2.20"

    id("org.cyclonedx.bom") version "3.0.0"
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io")

    maven {
        url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
}

apply(plugin = "com.diffplug.spotless")

spotless {
    kotlin {
        ktlint("1.7.1")
    }
}

configurations.all {
    resolutionStrategy {
        failOnNonReproducibleResolution()
    }
}

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-database-postgresql")

    // Logging
    implementation("net.logstash.logback:logstash-logback-encoder:8.1")

    implementation("io.micrometer:micrometer-registry-prometheus")

    implementation("no.nav.familie:prosessering-core:$familieProsesseringVersion")

    // Tillegggsstønader libs
    implementation("no.nav.tilleggsstonader-libs:util:$tilleggsstønaderLibsVersion")
    implementation("no.nav.tilleggsstonader-libs:log:$tilleggsstønaderLibsVersion")
    implementation("no.nav.tilleggsstonader-libs:http-client:$tilleggsstønaderLibsVersion")
    implementation("no.nav.tilleggsstonader-libs:sikkerhet:$tilleggsstønaderLibsVersion")

    implementation("no.nav.tilleggsstonader.kontrakter:kontrakter-felles:$tilleggsstønaderKontrakterVersion")

    // Kafka
    implementation("org.springframework.kafka:spring-kafka")
    implementation("no.nav.tms.varsel:kotlin-builder:2.1.1")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // Kun for å kunne bruke WebTestClient. Kan fjernes og erstattes av RestTestClient i spring-boot 4
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("com.github.tomakehurst:wiremock-jre8-standalone:$wiremockVersion")
    testImplementation("io.mockk:mockk:1.14.6")

    testImplementation("org.testcontainers:postgresql:$testcontainerVersion")

    testImplementation("no.nav.security:token-validation-spring-test:$tokenSupportVersion")
    testImplementation("no.nav.tilleggsstonader-libs:test-util:$tilleggsstønaderLibsVersion")

    // Transitiv avhengighet fra mock-oauth2-server -> bcpix. Disse under er definert som dynamisk versjon, noe bygget vårt ikke vil ha noe av
    testImplementation("org.bouncycastle:bcutil-jdk18on:1.82")
    testImplementation("org.bouncycastle:bcprov-jdk18on:1.82")
}

kotlin {
    jvmToolchain(javaVersion.asInt())

    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
}

application {
    mainClass.set("no.nav.tilleggsstonader.soknad.AppKt")
}

if (project.hasProperty("skipLint")) {
    gradle.startParameter.excludedTaskNames += "spotlessKotlinCheck"
}

tasks.test {
    useJUnitPlatform()
}

tasks.bootJar {
    archiveFileName.set("app.jar")
}

tasks.cyclonedxBom {
    includeConfigs.set(listOf("runtimeClasspath", "compileClasspath"))
}
