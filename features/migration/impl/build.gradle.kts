import extension.setupAnvil

/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
}

android {
    namespace = "io.element.android.features.migration.impl"
}

setupAnvil()

dependencies {
    implementation(projects.features.migration.api)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.preferences.impl)
    implementation(libs.androidx.datastore.preferences)
    implementation(projects.features.rageshake.api)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.sessionStorage.api)
    implementation(projects.libraries.uiStrings)

    testImplementation(libs.test.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.molecule.runtime)
    testImplementation(libs.test.robolectric)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.sessionStorage.implMemory)
    testImplementation(projects.libraries.sessionStorage.test)
    testImplementation(projects.libraries.preferences.test)
    testImplementation(projects.tests.testutils)
    testImplementation(projects.features.rageshake.test)
}
