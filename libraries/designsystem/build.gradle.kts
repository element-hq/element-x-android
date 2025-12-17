import extension.testCommonDependencies

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
}

dependencies {
    api(projects.libraries.compound)

    implementation(libs.androidx.compose.material3.windowsizeclass)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.coil.compose)
    implementation(libs.vanniktech.blurhash)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.core)
    implementation(projects.libraries.preferences.api)
    implementation(projects.libraries.testtags)
    implementation(projects.libraries.uiStrings)

    ksp(libs.showkase.processor)
    implementation(libs.showkase)

    testCommonDependencies(libs)
}
