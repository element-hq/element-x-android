/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.features.logout.test"
}

dependencies {
    implementation(libs.coroutines.core)
    implementation(projects.tests.testutils)
    api(projects.features.logout.api)
}
