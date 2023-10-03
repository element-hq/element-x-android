plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.features.${MODULE_NAME}.api"
}

dependencies {
    implementation(projects.libraries.architecture)
}
