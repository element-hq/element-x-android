plugins {
    id("io.element.android-compose-library")
    alias(libs.plugins.ksp)
    alias(libs.plugins.anvil)
}

android {
    namespace = "io.element.android.x.features.rageshake"
}

anvil {
    generateDaggerFactories.set(true)
}

dependencies {
    implementation(project(":libraries:core"))
    anvil(project(":anvilcodegen"))
    implementation(project(":libraries:di"))
    implementation(project(":anvilannotations"))
    implementation(project(":libraries:designsystem"))
    implementation(project(":libraries:elementresources"))
    implementation(libs.mavericks.compose)
    implementation(libs.squareup.seismic)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.coil)
    implementation(libs.coil.compose)
    ksp(libs.showkase.processor)
    testImplementation(libs.test.junit)
    androidTestImplementation(libs.test.junitext)
}
