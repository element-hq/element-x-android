/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.libraries.maplibre.compose"

    kotlinOptions {
        freeCompilerArgs += "-Xexplicit-api=strict"
    }
}

dependencies {
    api(libs.maplibre)
    api(libs.maplibre.ktx)
    api(libs.maplibre.annotation)
    // needed for libs.maplibre.annotation waiting for a new release with the fix
    implementation(libs.mapbox.android.gestures)
}
