plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.gf.common"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true"
                )
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
}

kapt {
    correctErrorTypes = true
}

dependencies {
    val room_version = "2.5.0"

    api(platform("com.google.firebase:firebase-bom:32.2.0"))
    api("com.google.firebase:firebase-analytics-ktx")
    api("com.google.firebase:firebase-firestore-ktx")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    api("androidx.navigation:navigation-fragment-ktx:2.6.0")
    api("androidx.navigation:navigation-ui-ktx:2.6.0")

    api ("androidx.appcompat:appcompat:1.6.1")
    api ("org.jetbrains.kotlin:kotlin-stdlib:1.8.22")
    api ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    api ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    api ("androidx.constraintlayout:constraintlayout:2.1.4")
    api ("androidx.recyclerview:recyclerview:1.3.1")
    api ("com.github.bumptech.glide:glide:4.11.0")

    // Room

    api("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    api("androidx.room:room-ktx:$room_version")
    api("com.google.code.gson:gson:2.9.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.44")
    kapt("com.google.dagger:hilt-android-compiler:2.44")

    // Viewmodel
    api("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    api("androidx.lifecycle:lifecycle-livedata:2.6.1")
    api("androidx.fragment:fragment-ktx:1.6.1")
    api("androidx.activity:activity-ktx:1.7.2")

    // SplashScreen
    api("androidx.core:core-splashscreen:1.0.1")

    // Google Maps
    api("com.google.android.gms:play-services-maps:18.1.0")
    api("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.maps.android:android-maps-utils:2.3.0")

    api(platform("com.google.firebase:firebase-bom:32.2.0"))

    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    api("com.google.firebase:firebase-auth-ktx")


}