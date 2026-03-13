import extension.setupDependencyInjection
import extension.testCommonDependencies

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
}

android {
    namespace = "io.element.android.features.location.impl"
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

setupDependencyInjection()

dependencies {
    api(projects.features.location.api)
    implementation(projects.features.messages.api)
    implementation(libs.maplibre.compose)
    implementation(libs.coil)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.di)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.core)
    implementation(projects.libraries.matrixui)
    implementation(projects.services.analytics.api)
    implementation(libs.accompanist.permission)
    implementation(projects.libraries.uiStrings)
    implementation(projects.libraries.featureflag.api)
    implementation(projects.libraries.dateformatter.api)

    testCommonDependencies(libs, true)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.dateformatter.test)
    testImplementation(projects.services.toolbox.test)
    testImplementation(projects.libraries.testtags)
    testImplementation(projects.services.analytics.test)
    testImplementation(projects.features.messages.test)
    testImplementation(projects.libraries.featureflag.test)
}
