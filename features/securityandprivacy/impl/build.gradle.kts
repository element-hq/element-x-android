import extension.setupDependencyInjection
import extension.testCommonDependencies

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.features.securityandprivacy.impl"

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

setupDependencyInjection()

dependencies {
    api(projects.features.securityandprivacy.api)
    implementation(projects.appnav)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.core)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.matrix.api)
    // For test fixtures used in previews
    implementation(projects.libraries.previewutils)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.uiStrings)
    implementation(projects.services.analytics.api)
    implementation(projects.libraries.featureflag.api)

    testCommonDependencies(libs, true)
    testImplementation(projects.services.analytics.test)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.featureflag.test)
    testImplementation(projects.libraries.testtags)
}
