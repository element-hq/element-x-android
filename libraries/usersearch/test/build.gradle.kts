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
    namespace = "io.element.android.libraries.usersearch"
}

dependencies {
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.matrix.api)
    api(projects.libraries.usersearch.api)
}
