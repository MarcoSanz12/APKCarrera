// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
    }
    dependencies {
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.5")
        classpath("com.android.tools.build:gradle:8.0.0")
    }
}
plugins {
    id("com.android.application") version "8.1.1"
    id("org.jetbrains.kotlin.android") version "1.8.0"
    id("com.google.gms.google-services") version "4.3.15"
    id("com.google.dagger.hilt.android") version "2.48.1"
    id("com.google.devtools.ksp") version "1.8.10-1.0.9"
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1"

}

