import extension.androidConfig
import extension.composeConfig
import extension.composeDependencies

plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    androidConfig()
    composeConfig()
}

dependencies {
    composeDependencies()
}
