val javaVersion = JavaVersion.VERSION_17

plugins {
    application

    kotlin("jvm") version "1.9.0"

    id("com.diffplug.spotless") version "6.20.0"

    id("org.springframework.boot") version "3.1.2"
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("plugin.spring") version "1.9.0"
}

repositories {
    mavenCentral()
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
    implementation("ch.qos.logback:logback-classic:1.4.11")
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

apply(plugin = "com.diffplug.spotless")

spotless {
    kotlin {
        //ktlint("0.50.0")
    }
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
}
