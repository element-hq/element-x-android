plugins {
    id("io.element.android-compose-library")
    alias(libs.plugins.ksp)
    alias(libs.plugins.anvil)
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.x.features.login"
}

anvil {
    generateDaggerFactories.set(true)
}


dependencies {
    implementation(project(":anvilannotations"))
    anvil(project(":anvilcodegen"))
    implementation(project(":libraries:di"))
    implementation(project(":libraries:core"))
    implementation(project(":libraries:architecture"))
    implementation(project(":libraries:matrix"))
    implementation(project(":libraries:designsystem"))
    implementation(project(":libraries:elementresources"))
    implementation(libs.appyx.core)
    implementation(libs.mavericks.compose)
    ksp(libs.showkase.processor)
    testImplementation(libs.test.junit)
    androidTestImplementation(libs.test.junitext)
}
