import java.util.Properties

plugins {
    id("com.vanniktech.maven.publish") version "0.9.0"
}

dependencies {
    implementation(kotlin("stdlib-jdk7"))

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.4")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.4")

    api("com.squareup.okhttp3:okhttp:4.4.0")
    api("net.mready.json:fluidjson:0.3.4")

    testImplementation("junit:junit:4.12")
    testImplementation(kotlin("test-junit"))
}

mavenPublish {
    releaseSigningEnabled = false
    targets {
        named("uploadArchives") {
            val properties = Properties()
            project.rootProject.file("local.properties").inputStream().use {
                properties.load(it)
            }

            releaseRepositoryUrl = "http://repo.mready.net/repository/android-releases/"
            snapshotRepositoryUrl = "http://repo.mready.net/repository/android-snapshots/"
            repositoryUsername = project.findProperty("username")?.toString()
                ?: properties.getProperty("NEXUS_USERNAME")
            repositoryPassword = project.findProperty("password")?.toString()
                ?: properties.getProperty("NEXUS_PASSWORD")
            signing = false
        }
    }
}

/*
  upload:

  ./gradlew clean build -Pusername=USER -Ppassword=PASSWORD uploadArchives
 */
