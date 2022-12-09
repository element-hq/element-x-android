import extension.androidConfig
import extension.commonDependencies
import extension.composeConfig
import extension.composeDependencies

plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    androidConfig(project)
    composeConfig()
}

dependencies {
    commonDependencies()
    composeDependencies()
}
