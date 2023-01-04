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
    implementation(project(":libraries:architecture"))
    implementation(libs.mavericks.compose)
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pagerindicator)
    implementation(libs.appyx.core)
    testImplementation(libs.test.junit)
    androidTestImplementation(libs.test.junitext)
    ksp(libs.showkase.processor)
}
