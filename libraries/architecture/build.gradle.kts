// TODO: Remove once https://youtrack.jetbrains.com/issue/KTIJ-19369 is fixed
@Suppress("DSL_SCOPE_VIOLATION")
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
}
