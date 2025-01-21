/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.features.joinroom.api"
}

dependencies {
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.matrix.api)
    implementation(projects.features.roomdirectory.api)
    implementation(projects.services.analytics.api)
}
