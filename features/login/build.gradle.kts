plugins {
    id("io.element.android-compose-library")
    alias(libs.plugins.ksp)
}

android {
    namespace = "io.element.android.x.features.login"
}

dependencies {
    implementation(project(":libraries:core"))
    implementation(project(":libraries:matrix"))
    implementation(project(":libraries:designsystem"))
    implementation(project(":libraries:elementresources"))
    implementation(libs.mavericks.compose)
    ksp(libs.showkase.processor)
    implementation(libs.timber)
    testImplementation(libs.test.junit)
    androidTestImplementation(libs.test.junitext)

}
