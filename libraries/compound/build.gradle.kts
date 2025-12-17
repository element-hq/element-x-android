import extension.testCommonDependencies

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "io.element.android.compound"

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(libs.showkase)
    testCommonDependencies(libs)
    testImplementation(libs.test.roborazzi)
    testImplementation(libs.test.roborazzi.compose)
    testImplementation(libs.test.roborazzi.junit)
}
