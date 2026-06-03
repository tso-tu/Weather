plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.example.weather"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.weather"
        minSdk = 21
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //implementations for https-requests
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    //implementation for json file
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    //implementation for map
    implementation("org.maplibre.gl:android-sdk:11.7.0")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
}