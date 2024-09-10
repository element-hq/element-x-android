/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */
plugins {
    id("io.element.android-library")
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.features.login.api"
}

dependencies {
    implementation(projects.libraries.architecture)
}
