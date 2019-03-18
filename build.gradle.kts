import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.21" apply false
}

allprojects {

    apply(plugin = "maven")

    group = "me.mozidev.keepdefault"
    version = "1.0"

    repositories {
        maven(url = "http://maven.aliyun.com/nexus/content/groups/public/")
        maven(url = "https://maven.aliyun.com/repository/google")
        maven(url = "https://jitpack.io")
    }

    tasks.withType<KotlinCompile>{
        kotlinOptions.jvmTarget = "1.8"
    }
}

configurations.all {
    resolutionStrategy.dependencySubstitution.all {
        requested.let {
            if (it is ModuleComponentSelector && it.group == "com.github.zijing07.gsonkeepdefault") {
                val targetProject = findProject(":${it.module}")
                if (targetProject != null) {
                    useTarget(targetProject)
                }
            }
        }
    }
}