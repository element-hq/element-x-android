plugins {
    id("io.element.android-compose-library")
    alias(libs.plugins.ksp)
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
    ksp(libs.showkase.processor)
}
