/**
 * This will generate the plugin "io.element.android-compose-application", used only in the module `app`.
 */
import extension.androidConfig
import extension.commonDependencies
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
    commonDependencies()
    composeDependencies()
}
