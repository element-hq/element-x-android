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
    namespace = "io.element.android.libraries.mediaviewer.api"
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

setupAnvil()

dependencies {
    implementation(libs.coil.compose)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.coroutines.core)
    implementation(libs.dagger)
    implementation(libs.telephoto.zoomableimage)
    implementation(libs.vanniktech.blurhash)
    implementation(libs.telephoto.flick)

    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.core)
    implementation(projects.libraries.di)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.uiStrings)

    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.mediaviewer.test)
    testImplementation(projects.tests.testutils)
    testImplementation(libs.test.junit)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.mockk)
    testImplementation(libs.test.robolectric)
    testImplementation(libs.test.turbine)
    testImplementation(libs.coroutines.core)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.androidx.compose.ui.test.junit)
    testReleaseImplementation(libs.androidx.compose.ui.test.manifest)
}
