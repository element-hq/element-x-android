plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.x.sdk.matrix"
}

dependencies {
    api(files("./libs/matrix-rust-sdk.aar"))
    implementation(project(":libraries:core"))
    implementation(libs.timber)
    implementation("net.java.dev.jna:jna:5.10.0@aar")
    implementation("androidx.datastore:datastore-core:1.0.0")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
}