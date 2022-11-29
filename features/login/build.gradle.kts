plugins {
    id("io.element.android-compose")
    // TODO Move to common config
    id("com.google.devtools.ksp") version "1.7.20-1.0.7"
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
    implementation(libs.timber)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    // TODO Move to common config
    ksp("com.airbnb.android:showkase-processor:1.0.0-beta14")
}