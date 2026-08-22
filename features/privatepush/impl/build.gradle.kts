import extension.setupDependencyInjection
import extension.testCommonDependencies

/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "io.element.android.features.privatepush.impl"

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

setupDependencyInjection()

dependencies {
    api(projects.features.privatepush.api)
    implementation(projects.appconfig)
    // Generalized APK downloader (sha256 + signing-cert pin + package/versionCode checks).
    implementation(projects.features.appupdate.api)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.core)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.di)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.preferences.api)
    implementation(projects.libraries.push.api)
    implementation(projects.libraries.pushproviders.api)
    // Built-in Feral provider: fallback/self-healing shared with the NoDistributors route.
    implementation(projects.libraries.pushproviders.feral)
    implementation(projects.libraries.troubleshoot.api)
    implementation(projects.libraries.uiStrings)
    implementation(libs.androidx.corektx)
    implementation(libs.androidx.datastore.preferences)
    implementation(platform(libs.network.okhttp.bom))
    implementation(libs.network.okhttp)
    implementation(libs.serialization.json)
    implementation(libs.coroutines.core)

    testCommonDependencies(libs, true)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.preferences.test)
    testImplementation(projects.libraries.push.test)
    testImplementation(projects.libraries.pushproviders.test)
    testImplementation(projects.libraries.sessionStorage.api)
    testImplementation(projects.libraries.sessionStorage.test)
    testImplementation(projects.features.privatepush.test)
}
