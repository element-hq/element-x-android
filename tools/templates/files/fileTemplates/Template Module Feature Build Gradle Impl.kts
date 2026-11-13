import extension.setupDependencyInjection
import extension.testCommonDependencies

plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.features.${MODULE_NAME}.impl"
}

setupDependencyInjection()

dependencies {
    api(projects.features.${MODULE_NAME}.api)
    implementation(projects.libraries.core)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.designsystem)

    testCommonDependencies(libs)
    testImplementation(projects.libraries.matrix.test)
}
