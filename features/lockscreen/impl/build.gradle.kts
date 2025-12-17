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
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.features.lockscreen.impl"

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

setupDependencyInjection()

dependencies {
    api(projects.features.lockscreen.api)
    implementation(projects.appconfig)
    implementation(projects.features.enterprise.api)
    implementation(projects.libraries.core)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.featureflag.api)
    implementation(projects.libraries.cryptography.api)
    implementation(projects.libraries.preferences.api)
    implementation(projects.libraries.testtags)
    implementation(projects.libraries.uiUtils)
    implementation(projects.features.logout.api)
    implementation(projects.libraries.uiCommon)
    implementation(projects.libraries.uiStrings)
    implementation(projects.libraries.sessionStorage.api)
    implementation(projects.services.appnavstate.api)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.biometric)

    testCommonDependencies(libs, true)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.cryptography.test)
    testImplementation(projects.libraries.cryptography.impl)
    testImplementation(projects.libraries.featureflag.test)
    testImplementation(projects.libraries.sessionStorage.test)
    testImplementation(projects.services.appnavstate.test)
    testImplementation(projects.features.logout.test)
}
