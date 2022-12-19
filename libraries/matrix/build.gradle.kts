plugins {
    id("io.element.android-library")
    alias(libs.plugins.anvil)
    kotlin("plugin.serialization") version "1.7.20"
}

android {
    namespace = "io.element.android.x.sdk.matrix"
}

anvil {
    generateDaggerFactories.set(true)
}

dependencies {
    api(project(":libraries:rustsdk"))
    implementation(project(":libraries:di"))
    implementation(project(":libraries:core"))
    implementation("net.java.dev.jna:jna:5.12.1@aar")
    implementation(libs.coil.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.serialization.json)
}
