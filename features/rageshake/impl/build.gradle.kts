import extension.setupAnvil

/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.features.rageshake.impl"

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

setupAnvil()

dependencies {
    implementation(projects.appconfig)
    implementation(projects.services.toolbox.api)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.core)
    implementation(projects.libraries.network)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.uiStrings)
    implementation(projects.libraries.sessionStorage.api)
    implementation(projects.libraries.matrix.api)
    api(libs.squareup.seismic)
    api(projects.features.rageshake.api)
    implementation(libs.androidx.datastore.preferences)
    implementation(platform(libs.network.okhttp.bom))
    implementation(libs.network.okhttp.okhttp)
    implementation(libs.coil)
    implementation(libs.coil.compose)

    testImplementation(libs.test.junit)
    testImplementation(libs.test.robolectric)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.molecule.runtime)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(libs.test.mockk)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.sessionStorage.implMemory)
    testImplementation(projects.libraries.sessionStorage.test)
    testImplementation(projects.features.rageshake.test)
    testImplementation(projects.tests.testutils)
    testImplementation(projects.services.toolbox.test)
    testImplementation(libs.network.mockwebserver)
}
