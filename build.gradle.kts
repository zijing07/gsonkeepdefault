import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.21"
}

allprojects {

    apply(plugin = "maven")

    group = "me.mozidev.keepdefault"
    version = "1.0"

    repositories {
        maven(url = "http://maven.aliyun.com/nexus/content/groups/public/")
        maven(url = "https://maven.aliyun.com/repository/google")
    }

    tasks.withType<KotlinCompile>{
        kotlinOptions.jvmTarget = "1.8"
    }
}

repositories {
    maven(url = "https://jitpack.io")
}
