import extension.setupDependencyInjection
import extension.testCommonDependencies

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */
plugins {
    id("io.element.android-compose-library")
}

android {
    namespace = "io.element.android.libraries.voiceplayer.impl"
}

setupDependencyInjection()

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

    testCommonDependencies(libs)
    testImplementation(libs.coroutines.core)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.mediaplayer.test)
    testImplementation(projects.services.analytics.test)
}
