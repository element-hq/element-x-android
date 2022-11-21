plugins {
    id("io.element.android-library")
    kotlin("plugin.serialization") version "1.7.20"
}

android {
    namespace = "io.element.android.x.sdk.matrix"
}

dependencies {
    api(project(":libraries:rustSdk"))
    implementation(project(":libraries:core"))
    implementation(libs.timber)
    implementation("net.java.dev.jna:jna:5.10.0@aar")
    implementation(libs.coil.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.serialization.json)
}