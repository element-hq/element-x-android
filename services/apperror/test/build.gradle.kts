/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.services.apperror.test"
}

dependencies {
    implementation(libs.coroutines.core)
    implementation(projects.services.apperror.api)
    implementation(projects.tests.testutils)
}
