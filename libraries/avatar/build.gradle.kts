plugins {
    id("io.element.android-compose")
}

android {
    namespace = "io.element.android.x.libraries.avatar"
}

dependencies {
    implementation(project(":libraries:matrix"))
    implementation(libs.coil.compose)
}