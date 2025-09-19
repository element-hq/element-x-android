import extension.testCommonDependencies

/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.features.userprofile.shared"
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(projects.libraries.core)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.uiStrings)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.mediaviewer.api)
    implementation(projects.libraries.featureflag.api)
    implementation(projects.libraries.permissions.api)
    implementation(projects.libraries.preferences.api)
    implementation(projects.libraries.testtags)
    api(projects.features.userprofile.api)
    api(projects.services.apperror.api)
    implementation(libs.coil.compose)
    implementation(projects.features.startchat.api)
    implementation(projects.services.analytics.api)

    testCommonDependencies(libs, true)
    testImplementation(projects.libraries.matrix.test)
}
