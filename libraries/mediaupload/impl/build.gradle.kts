import extension.setupAnvil

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.libraries.mediaupload.impl"

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

setupAnvil()

dependencies {
    api(projects.libraries.mediaupload.api)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.core)
    implementation(projects.libraries.di)
    implementation(projects.libraries.matrix.api)
    implementation(projects.services.toolbox.api)
    implementation(libs.inject)
    implementation(libs.androidx.exifinterface)
    implementation(libs.coroutines.core)
    implementation(libs.otaliastudios.transcoder)
    implementation(libs.vanniktech.blurhash)

    testImplementation(libs.test.junit)
    testImplementation(libs.test.robolectric)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.test.truth)
    testImplementation(projects.tests.testutils)
    testImplementation(projects.services.toolbox.test)
}
