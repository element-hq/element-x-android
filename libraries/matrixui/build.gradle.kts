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
}

android {
    namespace = "io.element.android.libraries.matrix.ui"
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

setupAnvil()

dependencies {
    implementation(projects.libraries.di)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.core)
    implementation(projects.libraries.uiStrings)
    implementation(projects.libraries.tchaputils)
    implementation(projects.libraries.testtags)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.jsoup)

    testImplementation(libs.coroutines.test)
    testImplementation(libs.test.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.molecule.runtime)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.dateformatter.test)
    testImplementation(projects.tests.testutils)
    testImplementation(libs.test.mockk)
    testImplementation(libs.test.robolectric)
    testImplementation(libs.androidx.compose.ui.test.junit)
    testImplementation(projects.libraries.sessionStorage.test)
}
