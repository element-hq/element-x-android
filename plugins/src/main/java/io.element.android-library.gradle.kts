import extension.androidConfig
import extension.proguardConfig

plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    androidConfig()
    proguardConfig()
}
