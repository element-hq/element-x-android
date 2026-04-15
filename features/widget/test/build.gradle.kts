/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.features.widget.test"
}

dependencies {
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.core)

    api(projects.features.widget.api)
    implementation(projects.features.widget.impl)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.matrix.test)
    implementation(projects.tests.testutils)
}

