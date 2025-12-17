/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
}

android {
    namespace = "io.element.android.features.invitepeople.test"
}

dependencies {
    implementation(libs.coroutines.core)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.matrix.test)
    implementation(projects.libraries.architecture)
    implementation(projects.tests.testutils)
    api(projects.features.startchat.api)
}
