/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

import config.BuildTimeConfig
import extension.buildConfigFieldStr
import extension.readLocalProperty
import extension.testCommonDependencies

plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.features.location.api"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigFieldStr(
            name = "MAPTILER_BASE_URL",
            value = BuildTimeConfig.SERVICES_MAPTILER_BASE_URL ?: "https://api.maptiler.com/maps"
        )
        buildConfigFieldStr(
            name = "MAPTILER_API_KEY",
            value = if (isEnterpriseBuild) {
                BuildTimeConfig.SERVICES_MAPTILER_APIKEY
            } else {
                System.getenv("ELEMENT_ANDROID_MAPTILER_API_KEY")
                    ?: readLocalProperty("services.maptiler.apikey")
            }
                ?: ""
        )
        buildConfigFieldStr(
            name = "MAPTILER_LIGHT_MAP_ID",
            value = if (isEnterpriseBuild) {
                BuildTimeConfig.SERVICES_MAPTILER_LIGHT_MAPID
            } else {
                System.getenv("ELEMENT_ANDROID_MAPTILER_LIGHT_MAP_ID")
                    ?: readLocalProperty("services.maptiler.lightMapId")
            }
            // fall back to maptiler's default light map.
                ?: "basic-v2"
        )
        buildConfigFieldStr(
            name = "MAPTILER_DARK_MAP_ID",
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
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.uiStrings)
    implementation(libs.coil.compose)

    testCommonDependencies(libs)
}
