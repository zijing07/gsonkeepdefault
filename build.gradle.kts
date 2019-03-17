import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    base
    kotlin("jvm") version "1.3.21" apply false
}

allprojects {
    group = "me.mozidev.keepdefault"
    version = "1.0-SNAPSHOT"

    repositories {
        maven(url = "http://maven.aliyun.com/nexus/content/groups/public/")
        maven(url = "https://maven.aliyun.com/repository/google")
    }

    tasks.withType<KotlinCompile>{
        kotlinOptions.jvmTarget = "1.8"
    }
}
