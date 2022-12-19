plugins {
    id("io.element.android-compose-library")
    alias(libs.plugins.ksp)
}

android {
    namespace = "io.element.android.x.features.onboarding"
}

dependencies {
    implementation(project(":libraries:core"))
    implementation(project(":libraries:elementresources"))
    implementation(project(":libraries:designsystem"))
    implementation(libs.mavericks.compose)
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pagerindicator)
    testImplementation(libs.test.junit)
    androidTestImplementation(libs.test.junitext)
    ksp(libs.showkase.processor)
}
