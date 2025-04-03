/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

import config.BuildTimeConfig
import extension.readLocalProperty

plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.features.location.api"

    defaultConfig {
        resValue(
            type = "string",
            name = "maptiler_api_key",
            value = if (isEnterpriseBuild) {
                BuildTimeConfig.SERVICES_MAPTILER_APIKEY
            } else {
                System.getenv("ELEMENT_ANDROID_MAPTILER_API_KEY")
                    ?: readLocalProperty("services.maptiler.apikey")
            }
                ?: ""
        )
        resValue(
            type = "string",
            name = "maptiler_light_map_id",
            value = if (isEnterpriseBuild) {
                BuildTimeConfig.SERVICES_MAPTILER_LIGHT_MAPID
            } else {
                System.getenv("ELEMENT_ANDROID_MAPTILER_LIGHT_MAP_ID")
                    ?: readLocalProperty("services.maptiler.lightMapId")
            }
            // fall back to maptiler's default light map.
                ?: "basic-v2"
        )
        resValue(
            type = "string",
            name = "maptiler_dark_map_id",
            value = if (isEnterpriseBuild) {
                BuildTimeConfig.SERVICES_MAPTILER_DARK_MAPID
            } else {
                System.getenv("ELEMENT_ANDROID_MAPTILER_DARK_MAP_ID")
                    ?: readLocalProperty("services.maptiler.darkMapId")
            }
            // fall back to maptiler's default dark map.
                ?: "basic-v2-dark"
        )
    }
}

dependencies {
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.core)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.uiStrings)
    implementation(libs.coil.compose)

    testImplementation(libs.test.junit)
    testImplementation(libs.test.truth)
}
