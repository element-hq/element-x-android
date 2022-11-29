plugins {
    id("io.element.android-compose")
    // TODO Move to common config
    id("com.google.devtools.ksp") version "1.7.20-1.0.7"
}

android {
    namespace = "io.element.android.x.textcomposer"
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":libraries:elementresources"))
    implementation(project(":libraries:core"))
    implementation(libs.wysiwyg)
    implementation(libs.androidx.constraintlayout)
    implementation("com.google.android.material:material:1.7.0")
    // TODO Move to common config
    ksp("com.airbnb.android:showkase-processor:1.0.0-beta14")
}
