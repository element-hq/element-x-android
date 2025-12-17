import extension.setupDependencyInjection
import extension.testCommonDependencies

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
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

setupDependencyInjection()

dependencies {
    implementation(projects.appconfig)
    implementation(projects.features.enterprise.api)
    implementation(projects.features.viewfolder.api)
    implementation(projects.services.toolbox.api)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.core)
    implementation(projects.libraries.network)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.preferences.api)
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

    testCommonDependencies(libs)
    testImplementation(projects.features.enterprise.test)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.sessionStorage.test)
    testImplementation(projects.features.rageshake.test)
    testImplementation(projects.features.viewfolder.test)
    testImplementation(projects.libraries.preferences.test)
    testImplementation(projects.services.toolbox.test)
    testImplementation(libs.network.mockwebserver)
}
