/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

// Helpers to render the Composable previews in screenshot tests, using Paparazzi.
// Used by the module `tests:uitests`, and can be used by any module which needs its own screenshot tests.
plugins {
    id("io.element.android-compose-library")
}

android {
    namespace = "io.element.android.tests.screenshotutils"
}

dependencies {
    // Provided by the Paparazzi Gradle plugin, which must be applied by the modules running the screenshot tests.
    compileOnly(libs.test.paparazzi)
    api(libs.test.composable.preview.scanner)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.matrixui)
}
