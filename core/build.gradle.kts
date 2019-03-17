plugins {
    kotlin("jvm")
    kotlin("kapt")
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.2")
    implementation(kotlin("stdlib"))
    
    kaptTest(project(":processor"))
    testImplementation(project(":gsonkeepdefault-processor"))
    testImplementation("junit", "junit", "4.12")
}
