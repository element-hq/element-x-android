/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.libraries.fullscreenintent.api"
}

dependencies {
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.permissions.api)
    implementation(projects.libraries.preferences.api)
}
