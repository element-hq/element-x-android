import extension.setupAnvil

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.libraries.textcomposer"
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

setupAnvil()

dependencies {
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.uiStrings)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.core)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.testtags)
    implementation(projects.libraries.uiUtils)

    releaseApi(libs.matrix.richtexteditor)
    releaseApi(libs.matrix.richtexteditor.compose)
    if (file("${rootDir.path}/libraries/textcomposer/lib/library-compose.aar").exists()) {
        println("\nNote: Using local binaries of the Rich Text Editor.\n")
        debugApi(projects.libraries.textcomposer.lib)
    } else {
        debugApi(libs.matrix.richtexteditor)
        debugApi(libs.matrix.richtexteditor.compose)
    }

    testImplementation(libs.test.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.molecule.runtime)
    testImplementation(libs.test.robolectric)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.tests.testutils)
    testImplementation(libs.androidx.compose.ui.test.junit)
    testReleaseImplementation(libs.androidx.compose.ui.test.manifest)
}
