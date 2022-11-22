
plugins {
    id("com.vanniktech.maven.publish") version "0.22.0"
    id("org.jetbrains.dokka") version "1.7.20"
}

dependencies {
    implementation(kotlin("stdlib-jdk7"))

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    api("com.squareup.okhttp3:okhttp:4.10.0")
    api("net.mready.json:fluidjson:1.0.0")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation(kotlin("test-junit"))
}