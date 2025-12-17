/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */
plugins {
    id("io.element.android-compose-library")
}

android {
    namespace = "io.element.android.tests.testutils"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.test.junit)
    implementation(libs.test.truth)
    implementation(libs.coroutines.test)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.core)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.uiStrings)
    implementation(projects.services.toolbox.api)
    implementation(libs.test.turbine)
    implementation(libs.molecule.runtime)
    implementation(libs.androidx.compose.ui.test.junit)
}
