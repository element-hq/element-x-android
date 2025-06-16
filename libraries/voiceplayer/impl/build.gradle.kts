import extension.setupAnvil

/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */
plugins {
    id("io.element.android-compose-library")
}

android {
    namespace = "io.element.android.libraries.voiceplayer.impl"
}

setupAnvil()

dependencies {
    api(projects.libraries.voiceplayer.api)

    implementation(projects.libraries.audio.api)
    implementation(projects.libraries.core)
    implementation(projects.libraries.di)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.mediaplayer.api)
    implementation(projects.libraries.uiUtils)
    implementation(projects.services.analytics.api)

    implementation(libs.androidx.annotationjvm)
    implementation(libs.coroutines.core)

    testImplementation(libs.molecule.runtime)
    testImplementation(libs.test.junit)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.mockk)
    testImplementation(libs.test.turbine)
    testImplementation(libs.coroutines.core)
    testImplementation(libs.coroutines.test)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.mediaplayer.test)
    testImplementation(projects.services.analytics.test)
    testImplementation(projects.tests.testutils)
}
