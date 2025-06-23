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
    namespace = "io.element.android.libraries.oidc.test"
}

dependencies {
    implementation(libs.coroutines.core)
    api(projects.libraries.oidc.api)
    implementation(projects.tests.testutils)
}
