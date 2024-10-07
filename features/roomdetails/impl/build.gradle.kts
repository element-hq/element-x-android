import extension.setupAnvil

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.features.roomdetails.impl"
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

setupAnvil()

dependencies {
    implementation(projects.libraries.core)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.uiStrings)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.mediapickers.api)
    implementation(projects.libraries.mediaupload.api)
    implementation(projects.libraries.mediaviewer.api)
    implementation(projects.libraries.featureflag.api)
    implementation(projects.libraries.permissions.api)
    implementation(projects.libraries.preferences.api)
    implementation(projects.libraries.testtags)
    api(projects.features.roomdetails.api)
    api(projects.libraries.usersearch.api)
    api(projects.services.apperror.api)
    implementation(libs.coil.compose)
    implementation(projects.features.call.api)
    implementation(projects.features.createroom.api)
    implementation(projects.features.leaveroom.api)
    implementation(projects.features.userprofile.shared)
    implementation(projects.services.analytics.compose)
    implementation(projects.features.poll.api)
    implementation(projects.features.messages.api)

    testImplementation(libs.test.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.molecule.runtime)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(libs.test.mockk)
    testImplementation(libs.test.robolectric)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.mediaupload.test)
    testImplementation(projects.libraries.mediapickers.test)
    testImplementation(projects.libraries.permissions.test)
    testImplementation(projects.libraries.usersearch.test)
    testImplementation(projects.libraries.featureflag.test)
    testImplementation(projects.tests.testutils)
    testImplementation(projects.features.createroom.test)
    testImplementation(projects.services.analytics.test)
    testImplementation(libs.androidx.compose.ui.test.junit)
    testReleaseImplementation(libs.androidx.compose.ui.test.manifest)
}
