import org.jlleitschuh.gradle.ktlint.KtlintExtension

/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin library project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.4.2/userguide/building_java_projects.html
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.7.20"

    // Apply the java-library plugin for API and implementation separation.
    `java-library`
    `maven-publish`

    // linting
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
}

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

kotlin {
    jvmToolchain {
        this.languageVersion.set(JavaLanguageVersion.of(11))
    }
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.9.0")
    implementation("com.github.haroldadmin:NetworkResponseAdapter:5.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    // Jackson
    val jackson = "2.13.4.2"
    implementation("com.fasterxml.jackson.core:jackson-databind:$jackson")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jackson")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jackson")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jackson")

    // Auth header parsing
    implementation("im.toss:http-auth-parser:0.1.2")
}

configure<KtlintExtension> {
    version.set("0.45.2")
    enableExperimentalRules.set(true)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "de.cmdjulian"
            artifactId = "distribution"
            version = "1.0.1"

            from(components["java"])
        }
    }
}
