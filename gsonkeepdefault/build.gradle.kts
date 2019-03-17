import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.21"
}

group = "me.mozidev.keepdefault"
version = "1.0-SNAPSHOT"

repositories {
    maven(url = "http://maven.aliyun.com/nexus/content/groups/public/")
    maven(url = "https://maven.aliyun.com/repository/google")
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.2")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("junit", "junit", "4.12")
}

tasks.withType<KotlinCompile>{
    kotlinOptions.jvmTarget = "1.8"
}
