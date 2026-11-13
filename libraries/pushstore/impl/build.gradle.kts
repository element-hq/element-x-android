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
    namespace = "io.element.android.libraries.pushstore.impl"
}

setupDependencyInjection()

dependencies {
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.core)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.preferences.api)
    implementation(projects.libraries.pushstore.api)
    implementation(libs.androidx.corektx)
    implementation(libs.androidx.datastore.preferences)

    testCommonDependencies(libs)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.preferences.test)
    testImplementation(projects.libraries.pushstore.test)
}
