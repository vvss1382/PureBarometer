plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "kz.purebarometer"
    compileSdk = 35

    defaultConfig {
        applicationId = "kz.purebarometer"
        minSdk = 31
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"
    }

    buildFeatures {
        viewBinding = false
    }
}
