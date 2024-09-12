/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

import java.util.Properties

plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
}

fun readLocalProperty(name: String): String? = Properties().apply {
    try {
        load(rootProject.file("local.properties").reader())
    } catch (ignored: java.io.IOException) {
    }
}.getProperty(name)

android {
    namespace = "io.element.android.features.location.api"

    defaultConfig {
        resValue(
            type = "string",
            name = "maptiler_api_key",
            value = System.getenv("ELEMENT_ANDROID_MAPTILER_API_KEY")
                ?: readLocalProperty("services.maptiler.apikey")
                ?: ""
        )
        resValue(
            type = "string",
            name = "maptiler_light_map_id",
            value = System.getenv("ELEMENT_ANDROID_MAPTILER_LIGHT_MAP_ID")
                ?: readLocalProperty("services.maptiler.lightMapId")
                // fall back to maptiler's default light map.
                ?: "basic-v2"
        )
        resValue(
            type = "string",
            name = "maptiler_dark_map_id",
            value = System.getenv("ELEMENT_ANDROID_MAPTILER_DARK_MAP_ID")
                ?: readLocalProperty("services.maptiler.darkMapId")
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
