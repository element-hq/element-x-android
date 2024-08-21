plugins {
    id("io.element.android-compose-library")
}

android {
    namespace = "fr.gouv.tchap.libraries.tchaputils"
}

dependencies {
    testImplementation(libs.test.junit)
}
