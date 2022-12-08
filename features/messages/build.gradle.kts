plugins {
    id("io.element.android-compose-library")
    alias(libs.plugins.ksp)
}

android {
    namespace = "io.element.android.x.features.messages"
}

dependencies {
    implementation(project(":libraries:core"))
    implementation(project(":libraries:matrix"))
    implementation(project(":libraries:designsystem"))
    implementation(project(":libraries:textcomposer"))
    implementation(libs.mavericks.compose)
    implementation(libs.coil.compose)
    implementation(libs.timber)
    implementation(libs.datetime)
    implementation(libs.accompanist.flowlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.jsoup)
    testImplementation(libs.test.junit)
    androidTestImplementation(libs.test.junitext)
    ksp(libs.showkase.processor)
}
