import extension.setupAnvil

/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
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

setupAnvil()

dependencies {
    api(projects.features.location.api)
    implementation(projects.features.messages.api)
    implementation(projects.libraries.maplibreCompose)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.di)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.core)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.androidutils)
    implementation(projects.services.analytics.api)
    implementation(libs.accompanist.permission)
    implementation(projects.libraries.uiStrings)
    implementation(libs.dagger)

    testImplementation(libs.test.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.molecule.runtime)
    testImplementation(libs.test.robolectric)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.testtags)
    testImplementation(projects.services.analytics.test)
    testImplementation(projects.features.messages.test)
    testImplementation(projects.tests.testutils)
    testImplementation(libs.androidx.compose.ui.test.junit)
    testReleaseImplementation(libs.androidx.compose.ui.test.manifest)
}
