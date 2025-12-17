/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.libraries.push.test"
}

dependencies {
    api(projects.libraries.push.api)
    api(projects.libraries.pushproviders.api)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.push.impl)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.matrixui)
    implementation(projects.tests.testutils)
    implementation(libs.androidx.core)
    implementation(libs.coil.compose)
    implementation(libs.coil.test)
    implementation(libs.test.robolectric)
}
