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
    namespace = "io.element.android.libraries.mediaplayer.impl"
}

setupAnvil()

dependencies {
    api(projects.libraries.mediaplayer.api)
    implementation(libs.androidx.media3.exoplayer)

    implementation(libs.dagger)
    implementation(projects.libraries.audio.api)
    implementation(projects.libraries.core)
    implementation(projects.libraries.di)

    implementation(libs.coroutines.core)

    testImplementation(projects.tests.testutils)
    testImplementation(projects.libraries.audio.test)
    testImplementation(libs.test.junit)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.mockk)
    testImplementation(libs.test.turbine)
    testImplementation(libs.coroutines.core)
    testImplementation(libs.coroutines.test)
}
