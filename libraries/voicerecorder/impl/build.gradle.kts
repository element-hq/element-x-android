import extension.setupDependencyInjection

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
    namespace = "io.element.android.libraries.voicerecorder.impl"
}

setupDependencyInjection()

dependencies {
    api(projects.libraries.voicerecorder.api)
    api(libs.opusencoder)

    implementation(projects.appconfig)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.core)
    implementation(projects.libraries.di)

    implementation(libs.androidx.annotationjvm)
    implementation(libs.coroutines.core)

    testImplementation(projects.tests.testutils)
    testImplementation(libs.test.junit)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.mockk)
    testImplementation(libs.test.turbine)
    testImplementation(libs.coroutines.core)
    testImplementation(libs.coroutines.test)
}
