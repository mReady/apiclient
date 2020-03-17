
plugins {
    kotlin("jvm") version "1.3.70" apply false
    kotlin("plugin.serialization") version "1.3.70" apply false
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "kotlinx-serialization")

    repositories {
        jcenter()
        maven(url = "http://repo.mready.net/repository/android-releases/")
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