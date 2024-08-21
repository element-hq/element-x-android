/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
    alias(libs.plugins.ksp)
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.libraries.designsystem"

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        getByName("release") {
            consumerProguardFiles("consumer-rules.pro")
        }
    }

    dependencies {
        api(libs.compound)

        implementation(libs.androidx.compose.material3.windowsizeclass)
        implementation(libs.androidx.compose.material3.adaptive)
        implementation(libs.coil.compose)
        implementation(libs.vanniktech.blurhash)
        implementation(projects.libraries.architecture)
        implementation(projects.libraries.preferences.api)
        implementation(projects.libraries.tchaputils)
        implementation(projects.libraries.testtags)
        implementation(projects.libraries.uiStrings)

        ksp(libs.showkase.processor)

        testImplementation(libs.test.junit)
        testImplementation(libs.coroutines.test)
        testImplementation(libs.molecule.runtime)
        testImplementation(libs.test.truth)
        testImplementation(libs.test.turbine)
    }
}
