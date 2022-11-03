plugins {
    id("io.element.android-compose")
}

android {
    namespace = "io.element.android.x.libraries.designsystem"

    dependencies {
        // Should not be there, but this is a POC
        implementation("io.coil-kt:coil-compose:2.2.1")
        implementation(libs.accompanist.systemui)
    }
}