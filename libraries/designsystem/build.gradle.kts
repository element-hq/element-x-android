plugins {
    id("io.element.android-compose-library")
    alias(libs.plugins.ksp)
}

android {
    namespace = "io.element.android.x.libraries.designsystem"

    dependencies {
        // Should not be there, but this is a POC
        implementation(libs.coil.compose)
        implementation(libs.accompanist.systemui)
        implementation(project(":libraries:elementresources"))
        ksp(libs.showkase.processor)
    }
}
