import extension.androidConfig
import extension.commonDependencies

plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    androidConfig(project)
}

dependencies {
    commonDependencies()
}
