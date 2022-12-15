plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kapt)
}

dependencies {
    implementation(project(":anvilannotations"))
    api(libs.anvil.compiler.api)
    implementation(libs.anvil.compiler.utils)
    implementation("com.squareup:kotlinpoet:1.10.2")
    implementation(libs.dagger)
    compileOnly("com.google.auto.service:auto-service-annotations:1.0.1")
    kapt("com.google.auto.service:auto-service:1.0.1")
}