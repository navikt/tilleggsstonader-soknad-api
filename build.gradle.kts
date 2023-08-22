val javaVersion = JavaVersion.VERSION_17

plugins {
    application

    kotlin("jvm") version "1.9.0"

    id("com.diffplug.spotless") version "6.20.0"
}

repositories {
    mavenCentral()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(kotlin("stdlib"))
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
        ktlint("0.50.0")
    }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = javaVersion.toString()
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = javaVersion.toString()
        kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }
}
