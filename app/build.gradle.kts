plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
    id ("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id ("androidx.navigation.safeargs.kotlin")
    id("com.google.firebase.crashlytics")

}
android {
    namespace = "com.gf.apkcarrera"
    compileSdk = 34



    defaultConfig {
        resourceConfigurations += listOf("es", "en", "fr", "ca")
        applicationId = "com.gf.apkcarrera"
        minSdk = 26
        //noinspection EditedTargetSdkVersion
        targetSdk = 34
        versionCode = 3
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }


    }

    dataBinding{
        enable = true
    }

    buildFeatures {
        viewBinding = true
        compose = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
    kotlinOptions {
        jvmTarget = "18"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    /* kapt {
         correctErrorTypes = true
     }*/

}



dependencies {

    val room_version = "2.6.1"

    implementation("com.google.gms:google-services:4.3.15")
    implementation (project(":common"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    kapt("com.google.dagger:hilt-android-compiler:2.48.1")
    implementation ("com.google.dagger:hilt-android:2.48.1")

    // Expandable Layout
    kapt ("androidx.lifecycle:lifecycle-compiler:2.6.2")
    implementation("net.cachapa.expandablelayout:expandablelayout:2.9.2")

    kapt("androidx.room:room-compiler:$room_version")
}