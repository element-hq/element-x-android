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
    namespace = "io.element.android.libraries.mediapickers.test"

    dependencies {
        implementation(projects.libraries.core)
        implementation(projects.libraries.di)
        implementation(libs.inject)
        api(projects.libraries.mediapickers.api)
    }
}
