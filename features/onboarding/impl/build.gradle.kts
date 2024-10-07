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
    namespace = "io.element.android.features.onboarding.impl"

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

setupAnvil()

dependencies {
    implementation(projects.appconfig)
    implementation(projects.libraries.core)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.featureflag.api)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.testtags)
    implementation(projects.libraries.uiStrings)
    api(projects.features.onboarding.api)

    testImplementation(libs.test.junit)
    testImplementation(libs.androidx.compose.ui.test.junit)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.molecule.runtime)
    testImplementation(libs.test.robolectric)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.featureflag.test)
    testImplementation(projects.tests.testutils)
    testReleaseImplementation(libs.androidx.compose.ui.test.manifest)
}
