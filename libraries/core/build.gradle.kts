plugins {
    id("io.element.android-compose-library")
}

android {
    namespace = "io.element.android.x.core"
}

dependencies {
    api(libs.mavericks.compose)
    api(libs.dagger)
    api(libs.androidx.fragment)
    api(libs.appyx.core)
}
