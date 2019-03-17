plugins {
    kotlin("jvm")
    kotlin("kapt")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":core"))
    implementation("com.squareup:kotlinpoet:1.0.0-RC1")
    implementation("com.google.auto.service:auto-service:1.0-rc4")
    kapt("com.google.auto.service:auto-service:1.0-rc4")
    implementation("me.eugeniomarletti.kotlin.metadata:kotlin-metadata:1.4.0")

    kaptTest(project(":processor"))
    testImplementation("junit", "junit", "4.12")
}
