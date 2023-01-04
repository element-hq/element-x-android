plugins {
    id("io.element.android-library")
    alias(libs.plugins.molecule)
}

android {
    namespace = "io.element.android.x.libraries.presentation"
}

dependencies {
    api(libs.dagger)
    api(libs.appyx.core)
    api(libs.androidx.lifecycle.runtime)
}
