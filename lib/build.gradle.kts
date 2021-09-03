import java.util.Properties

dependencies {
    implementation(kotlin("stdlib-jdk7"))

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.1")

    api("com.squareup.okhttp3:okhttp:4.9.1")
    api("net.mready.json:fluidjson:1.0.0-beta7")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")

    testImplementation("junit:junit:4.12")
    testImplementation(kotlin("test-junit"))
}
