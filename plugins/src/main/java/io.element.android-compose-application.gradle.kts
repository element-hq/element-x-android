import extension.androidConfig
import extension.composeConfig
import extension.composeDependencies

plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    androidConfig(project)
    composeConfig()
}

dependencies {
    composeDependencies()
}
