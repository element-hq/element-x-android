plugins {
    alias(libs.plugins.kotlin.jvm)
}
java {
    sourceCompatibility = Versions.javaVersion
    targetCompatibility = Versions.javaVersion
}

kotlin {
    jvmToolchain {
        languageVersion = Versions.javaLanguageVersion
    }
}

dependencies {
    compileOnly(libs.test.detekt.api)
    testImplementation(libs.test.detekt.test)

    testImplementation(libs.test.truth)
}
