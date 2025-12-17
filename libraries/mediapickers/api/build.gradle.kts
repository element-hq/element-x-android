import extension.testCommonDependencies

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
}

android {
    namespace = "io.element.android.libraries.mediapickers.api"
}

dependencies {
    implementation(projects.libraries.uiStrings)
    implementation(projects.libraries.core)
    implementation(projects.libraries.di)

    testCommonDependencies(libs)
}
