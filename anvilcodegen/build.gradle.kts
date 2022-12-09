plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kapt)
}

/*
tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile).configureEach {
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-opt-in=com.squareup.anvil.annotations.ExperimentalAnvilApi")
    }
}

 */

dependencies {
    implementation(project(":anvilannotations"))
    api(libs.anvil.compiler.api)
    implementation(libs.anvil.compiler.utils)
    implementation("com.squareup:kotlinpoet:1.10.2")
    implementation(libs.dagger)
    compileOnly("com.google.auto.service:auto-service-annotations:1.0.1")
    kapt("com.google.auto.service:auto-service:1.0.1")
}