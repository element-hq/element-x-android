import extension.setupAnvil

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
}

setupAnvil()

android {
    namespace = "io.element.android.libraries.mediapickers.api"

    dependencies {
        implementation(projects.libraries.uiStrings)
        implementation(projects.libraries.core)
        implementation(projects.libraries.di)
        implementation(libs.inject)

        testImplementation(libs.test.junit)
        testImplementation(libs.coroutines.test)
        testImplementation(libs.test.truth)
        testImplementation(libs.test.robolectric)
    }
}
