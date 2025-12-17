import extension.setupDependencyInjection
import extension.testCommonDependencies

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */
plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.libraries.mediaplayer.impl"
}

setupDependencyInjection()

dependencies {
    api(projects.libraries.mediaplayer.api)
    implementation(libs.androidx.media3.exoplayer)

    implementation(projects.libraries.audio.api)
    implementation(projects.libraries.core)
    implementation(projects.libraries.di)

    implementation(libs.coroutines.core)

    testCommonDependencies(libs)
    testImplementation(projects.libraries.audio.test)
    testImplementation(libs.coroutines.core)
}
