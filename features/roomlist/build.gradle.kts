plugins {
    id("io.element.android-compose-library")
    alias(libs.plugins.ksp)
    alias(libs.plugins.anvil)
}

android {
    namespace = "io.element.android.x.features.roomlist"
}

anvil {
    generateDaggerFactories.set(true)
}

dependencies {
    implementation(project(":anvilannotations"))
    anvil(project(":anvilcodegen"))
    implementation(project(":libraries:di"))
    implementation(project(":libraries:core"))
    implementation(project(":libraries:matrix"))
    implementation(project(":libraries:designsystem"))
    implementation(project(":libraries:elementresources"))
    implementation(libs.mavericks.compose)
    implementation(libs.datetime)
    implementation(libs.accompanist.placeholder)
    testImplementation(libs.test.junit)
    androidTestImplementation(libs.test.junitext)
    ksp(libs.showkase.processor)
}
