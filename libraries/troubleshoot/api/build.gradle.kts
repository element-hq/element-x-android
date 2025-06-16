/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */
plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.libraries.troubleshoot.api"
}

dependencies {
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.matrix.api)
    implementation(libs.androidx.corektx)
    implementation(libs.coroutines.core)
}
