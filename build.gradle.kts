plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.android.library) apply false
}

buildscript {
    dependencies {
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.android.gradle.plugin)
    }
}