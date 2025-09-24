import extension.setupDependencyInjection
import extension.testCommonDependencies

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-library")
}

setupDependencyInjection()

android {
    namespace = "io.element.android.libraries.mediaupload.api"
}

dependencies {
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.core)
    implementation(projects.libraries.di)
    api(projects.libraries.matrix.api)
    api(projects.libraries.preferences.api)
    implementation(libs.inject)
    implementation(libs.coroutines.core)

    testCommonDependencies(libs)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.preferences.test)
    testImplementation(projects.libraries.mediaupload.test)
}
