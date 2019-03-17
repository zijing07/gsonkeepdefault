import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.21"
    kotlin("kapt") version "1.3.21"
}

group = "me.mozidev.keepdefault"
version = "1.0-SNAPSHOT"

repositories {
    maven(url = "http://maven.aliyun.com/nexus/content/groups/public/")
    maven(url = "https://maven.aliyun.com/repository/google")
}

dependencies {
    implementation ("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation ("com.squareup:kotlinpoet:1.0.0-RC1")
    implementation ("com.google.auto.service:auto-service:1.0-rc4")
    implementation ("me.eugeniomarletti.kotlin.metadata:kotlin-metadata:1.4.0")
    testImplementation("junit", "junit", "4.12")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
