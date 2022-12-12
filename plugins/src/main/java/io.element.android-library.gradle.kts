/**
 * This will generate the plugin "io.element.android-library", used in android library without compose modules.
 */
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
