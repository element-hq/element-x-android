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
    namespace = "io.element.android.libraries.voicerecorder.test"
}

dependencies {
    api(projects.libraries.voicerecorder.api)
    implementation(projects.tests.testutils)

    implementation(libs.coroutines.test)
    implementation(libs.test.truth)
    implementation(projects.libraries.core)
}
