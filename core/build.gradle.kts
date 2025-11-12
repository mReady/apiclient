import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.vaniktek.mavem.publish)
    alias(libs.plugins.jetbrains.dokka)
//    id("maven-publish")
}

group = "net.mready.apiclient"
version = "1.1.1"

mavenPublishing {
    //For publishing to mavenLocal comment the next line, uncomment the id("maven-publish") plugin
    // sync with gradle and run the task publishToMavenLocal
    signAllPublications()

    publishToMavenCentral()
    coordinates(groupId = group.toString(), artifactId = "core", version = version.toString())

    pom {
        name = "Api Client"
        description = "An easy-to-use library for handling HTTP requests built on top of Ktor and FluidJson"
        url = "https://github.com/mready/apiclient/"

        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "mready"
                name = "mReady"
                organization = "mReady"
                organizationUrl = "https://github.com/mready/"
                email = "team@mready.net"
                url = "https://github.com/mready/"
            }
        }
        scm {
            url = "https://github.com/mready/apiclient/"
            connection = "scm:git:git://github.com/mready/apiclient.git"
            developerConnection = "scm:git:ssh://git@github.com/mready/apiclient.git"
        }
    }
}

kotlin {
    androidLibrary {
        namespace = "net.mready.apiclient"
        minSdk = libs.versions.minSdk.get().toInt()
        compileSdk = libs.versions.targetSdk.get().toInt()

        withJava()
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        //Also update in publish.yml !!
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
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
                api(libs.ktor.client.contentNegotiation)
                api(libs.ktor.serialization.json)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.ktor.client.cio)
                implementation(kotlin("test"))
            }
        }
    }
}
