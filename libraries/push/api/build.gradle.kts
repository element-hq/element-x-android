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
    namespace = "io.element.android.libraries.push.api"
}

dependencies {
    implementation(libs.androidx.corektx)
    implementation(libs.coroutines.core)
    implementation(libs.coil.compose)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.pushproviders.api)
}
