/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.features.poll.test"
}

dependencies {
    implementation(projects.libraries.matrix.api)
    api(projects.features.poll.api)
    implementation(libs.kotlinx.collections.immutable)
    implementation(projects.tests.testutils)
}
