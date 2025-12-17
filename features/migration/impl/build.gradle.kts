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
    namespace = "io.element.android.features.migration.impl"
}

setupDependencyInjection()

dependencies {
    implementation(projects.features.announcement.api)
    implementation(projects.features.migration.api)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.preferences.impl)
    implementation(libs.androidx.datastore.preferences)
    implementation(projects.features.rageshake.api)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.sessionStorage.api)
    implementation(projects.libraries.uiStrings)

    testCommonDependencies(libs)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.sessionStorage.test)
    testImplementation(projects.libraries.preferences.test)
    testImplementation(projects.features.announcement.test)
    testImplementation(projects.features.rageshake.test)
}
