import extension.setupDependencyInjection
import extension.testCommonDependencies

/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.features.mediapreview.impl"
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

setupDependencyInjection()

dependencies {
    api(projects.features.mediapreview.api)
    implementation(projects.appconfig)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.core)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.mediaupload.api)
    implementation(projects.libraries.mediaupload.impl)
    implementation(projects.libraries.mediaviewer.api)
    implementation(projects.libraries.textcomposer.impl)
    implementation(projects.libraries.uiStrings)
    implementation(projects.libraries.preferences.api)
    implementation(projects.libraries.di)
    implementation(projects.libraries.uiUtils)
    implementation(projects.libraries.featureflag.api)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.timber)

    testCommonDependencies(libs, true)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.mediaupload.test)
    testImplementation(projects.libraries.preferences.test)
    testImplementation(projects.libraries.mediaviewer.test)
    testImplementation(projects.libraries.featureflag.test)
}
