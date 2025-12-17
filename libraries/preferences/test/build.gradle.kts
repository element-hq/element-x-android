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
    namespace = "io.element.android.libraries.preferences.test"
}

dependencies {
    api(projects.libraries.preferences.api)
    implementation(projects.libraries.matrix.api)
    implementation(projects.tests.testutils)
    implementation(libs.coroutines.core)
    implementation(libs.androidx.datastore.preferences)
}
