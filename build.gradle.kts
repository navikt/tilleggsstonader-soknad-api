val javaVersion = JavaVersion.VERSION_17

plugins {
    kotlin("jvm") version "1.9.0"

    application
}

repositories {
    mavenCentral()
}

dependencies {
}

java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

application {
    mainClass.set("no.nav.tilleggsstonader.soknad.AppKt")
}

tasks {
}
