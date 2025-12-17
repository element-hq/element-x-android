/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.libraries.maplibre.compose"

    kotlin {
        compilerOptions {
            explicitApi()
        }
    }
}

dependencies {
    api(libs.maplibre)
    api(libs.maplibre.ktx)
    api(libs.maplibre.annotation)
}
