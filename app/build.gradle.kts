plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp")
    id ("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")

}



android {
    namespace = "com.gf.apkcarrera"
    compileSdk = 34



    defaultConfig {
        applicationId = "com.gf.apkcarrera"
        minSdk = 26
        //noinspection EditedTargetSdkVersion
        targetSdk = 34
        versionCode = 1
        versionName = "0.0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }


    }


    buildFeatures {
        viewBinding = true
        compose = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
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

    val room_version = "2.5.0"

    implementation("com.google.gms:google-services:4.3.15")
    implementation (project(":common"))
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-firestore-ktx:24.7.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    kapt("com.google.dagger:hilt-android-compiler:2.44")
    implementation ("com.google.dagger:hilt-android:2.44")

    // Expandable Layout
    implementation("net.cachapa.expandablelayout:expandablelayout:2.9.2")

    ksp("androidx.room:room-compiler:$room_version")
}