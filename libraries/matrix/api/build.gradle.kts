import extension.setupAnvil

/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "io.element.android.libraries.matrix.api"

    buildFeatures {
        buildConfig = true
    }
}

setupAnvil()

dependencies {
    implementation(projects.libraries.di)
    implementation(projects.libraries.tchaputils)
    implementation(libs.dagger)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.core)
    implementation(projects.services.analytics.api)
    implementation(libs.serialization.json)
    api(projects.libraries.sessionStorage.api)
    implementation(libs.coroutines.core)
    api(projects.libraries.architecture)

    testImplementation(libs.test.junit)
    testImplementation(libs.test.truth)
    testImplementation(projects.libraries.matrix.test)
}
