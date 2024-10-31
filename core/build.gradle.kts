plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
//    alias(libs.plugins.vaniktek.mavem.publish)
    alias(libs.plugins.jetbrains.dokka)
    alias(libs.plugins.android.library)
    id("maven-publish")
}

group = "net.mready.apiclient"
version = "1.1.0-beta01"

publishing {
    repositories {
        mavenLocal()
    }
}

kotlin {
    androidTarget {
        publishLibraryVariants("release", "debug")
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlinx.coroutines.core)
                api(libs.mready.json)
                api(libs.kotlinx.serialization.json)

                //Ktor
                api(libs.ktor.client.core)
                api(libs.ktor.client.cio)
                api(libs.ktor.client.contentNegotiation)
                api(libs.ktor.serialization.json)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

android {
    namespace = "net.mready.apiclient"
    compileSdk = 34

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
