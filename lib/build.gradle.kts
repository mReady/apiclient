
plugins {
    id("com.vanniktech.maven.publish") version "0.14.2"
    id("org.jetbrains.dokka") version "1.5.0"
}

val GROUP: String by project
val VERSION_NAME: String by project

group = GROUP
version = VERSION_NAME

dependencies {
    implementation(kotlin("stdlib-jdk7"))

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")

    api("com.squareup.okhttp3:okhttp:4.9.1")
    api("net.mready.json:fluidjson:1.0.0-beta7")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")

    testImplementation("junit:junit:4.12")
    testImplementation(kotlin("test-junit"))
}