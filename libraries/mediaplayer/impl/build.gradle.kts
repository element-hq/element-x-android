/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */
plugins {
    id("io.element.android-library")
    alias(libs.plugins.anvil)
}

android {
    namespace = "io.element.android.libraries.mediaplayer.impl"
}

anvil {
    generateDaggerFactories.set(true)
}

dependencies {
    api(projects.libraries.mediaplayer.api)
    implementation(libs.androidx.media3.exoplayer)

    implementation(libs.dagger)
    implementation(projects.libraries.di)

    implementation(libs.coroutines.core)

    testImplementation(projects.tests.testutils)
    testImplementation(libs.test.junit)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.mockk)
    testImplementation(libs.test.turbine)
    testImplementation(libs.coroutines.core)
    testImplementation(libs.coroutines.test)
}
