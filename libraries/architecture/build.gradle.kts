plugins {
    id("io.element.android-compose-library")
    alias(libs.plugins.molecule)
}

android {
    namespace = "io.element.android.x.libraries.presentation"
}

dependencies {
    api(project(":libraries:core"))
    api(libs.dagger)
    api(libs.appyx.core)
    api(libs.androidx.lifecycle.runtime)
    api(libs.mavericks.compose)
}
