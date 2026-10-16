plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.features.contentscanner.api"
}

dependencies {
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.matrixui)

    testImplementation(libs.coroutines.test)
}
