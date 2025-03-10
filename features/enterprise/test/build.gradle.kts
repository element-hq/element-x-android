/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */
plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.features.enterprise.test"
}

dependencies {
    api(projects.features.enterprise.api)
    implementation(libs.compound)
    implementation(projects.libraries.matrix.api)
    implementation(projects.tests.testutils)
}
