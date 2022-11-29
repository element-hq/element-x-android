plugins {
    id("io.element.android-compose")
    // TODO Move to common config
    id("com.google.devtools.ksp") version "1.7.20-1.0.7"
}

android {
    namespace = "io.element.android.x.libraries.designsystem"

    dependencies {
        // Should not be there, but this is a POC
        implementation("io.coil-kt:coil-compose:2.2.1")
        implementation(libs.accompanist.systemui)
        // TODO Move to common config
        ksp("com.airbnb.android:showkase-processor:1.0.0-beta14")
    }
}