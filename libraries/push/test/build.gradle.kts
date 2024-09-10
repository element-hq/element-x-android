/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.libraries.push.test"
}

dependencies {
    api(projects.libraries.push.api)
    implementation(projects.libraries.push.impl)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.pushproviders.api)
    implementation(projects.tests.testutils)
    implementation(libs.androidx.core)
    implementation(libs.coil.compose)
    implementation(libs.coil.test)
    implementation(libs.test.robolectric)
}
