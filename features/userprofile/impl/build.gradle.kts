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
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.features.userprofile.impl"
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

setupDependencyInjection()

dependencies {
    implementation(projects.libraries.core)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.uiStrings)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.mediaviewer.api)
    implementation(projects.features.call.api)
    implementation(projects.features.enterprise.api)
    implementation(projects.features.verifysession.api)
    api(projects.features.userprofile.api)
    api(projects.features.userprofile.shared)
    implementation(libs.coil.compose)
    implementation(projects.features.startchat.api)
    implementation(projects.services.analytics.api)

    testCommonDependencies(libs, true)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.mediaviewer.test)
    testImplementation(projects.features.call.test)
    testImplementation(projects.features.verifysession.test)
    testImplementation(projects.features.startchat.test)
    testImplementation(projects.features.enterprise.test)
}
