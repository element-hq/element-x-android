import extension.setupAnvil

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.features.invite.impl"
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

setupAnvil()

dependencies {
    api(projects.features.invite.api)
    implementation(libs.androidx.datastore.preferences)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.core)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.uiStrings)
    implementation(projects.services.analytics.api)
    implementation(projects.libraries.push.api)

    testImplementation(libs.test.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.molecule.runtime)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(libs.test.robolectric)
    testImplementation(projects.features.invite.test)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.push.test)
    testImplementation(projects.services.analytics.test)
    testImplementation(projects.tests.testutils)
    testImplementation(libs.androidx.compose.ui.test.junit)
    testReleaseImplementation(libs.androidx.compose.ui.test.manifest)
}
