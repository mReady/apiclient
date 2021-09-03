
plugins {
    kotlin("jvm") version "1.5.21" apply false
    kotlin("plugin.serialization") version "1.5.20" apply false
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "kotlinx-serialization")

    repositories {
        jcenter()
        maven(url = "https://repo.mready.net/repository/android-releases/")
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.freeCompilerArgs = listOf(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-XXLanguage:+InlineClasses",
            "-progressive"
        )
        kotlinOptions.jvmTarget = "1.8"
    }
}