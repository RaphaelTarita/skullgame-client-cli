import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = "1.9.22"
val ktorVersion = "2.3.8"
val mordantVersion = "2.3.0"
val jnativehookVersion = "2.2.2"
val logbackVersion = "1.4.14"
val skullgameCommonVersion = "1.0.2"

plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    id("io.gitlab.arturbosch.detekt") version "1.23.5"
    application
}

group = "com.rtarita.skull"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "com.rtarita.skull.client.cli.MainKt"
}

repositories {
    mavenCentral()
    mavenLocal() // needed for 'com.rtarita.skull:skullgame-common' dependency
}

dependencies {
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-websockets:$ktorVersion")

    implementation("com.github.ajalt.mordant:mordant:$mordantVersion")
    implementation("com.github.kwhat:jnativehook:$jnativehookVersion")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // in order to use this dependency, the skullgame-common repository needs to be checked out first and installed into the maven local repository.
    // to do so, invoke 'gradlew(.bat) publishToMavenLocal' in the skullgame-common project
    implementation("com.rtarita.skull:skullgame-common:$skullgameCommonVersion")
}

tasks.withType<KotlinCompile>().all {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

detekt {
    basePath = rootDir.toString()
}
