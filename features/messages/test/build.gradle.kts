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
    namespace = "io.element.android.features.messages.test"
}

dependencies {
    api(projects.features.messages.impl)
    implementation(projects.libraries.matrix.test)
    implementation(projects.libraries.mediaplayer.test)
    implementation(projects.libraries.mediaupload.test)
    implementation(projects.libraries.mediaviewer.api)
    implementation(projects.libraries.permissions.test)
    implementation(projects.libraries.preferences.api)
    implementation(projects.libraries.voicerecorder.test)
    implementation(projects.services.analytics.test)
    implementation(projects.tests.testutils)
    implementation(projects.libraries.mediaupload.impl)
}
