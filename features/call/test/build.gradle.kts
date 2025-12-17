/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.features.call.test"
}

dependencies {
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.core)

    api(projects.features.call.api)
    implementation(projects.features.call.impl)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.matrix.test)
    implementation(projects.tests.testutils)
}
