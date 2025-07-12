/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */
plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("androidApplicationCompose") {
            id = "io.element.android.compose.application"
            implementationClass = "AndroidComposeApplicationPlugin"
        }
        register("androidComposeLibrary") {
            id = "io.element.android.compose.library"
            implementationClass = "AndroidComposeLibraryPlugin"
        }
        register("androidLibrary") {
            id = "io.element.android.library"
            implementationClass = "AndroidLibraryPlugin"
        }
        register("rootProjectPlugin") {
            id = "io.element.android.root"
            implementationClass = "RootProjectPlugin"
        }
    }
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(libs.android.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kover.gradle.plugin)
    implementation(platform(libs.google.firebase.bom))
    implementation(libs.firebase.appdistribution.gradle)
    implementation(libs.autonomousapps.dependencyanalysis.plugin)
    implementation(libs.anvil.gradle.plugin)
    implementation(libs.ksp.gradle.plugin)
    implementation(libs.compose.compiler.plugin)
}
