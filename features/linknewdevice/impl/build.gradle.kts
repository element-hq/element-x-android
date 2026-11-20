import extension.setupDependencyInjection
import extension.testCommonDependencies

/*
 * Copyright (c) 2025 Element Creations Ltd.
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
    namespace = "io.element.android.features.linknewdevice.impl"

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

setupDependencyInjection()

dependencies {
    // TODO Cleanup
    implementation(projects.features.enterprise.api)
    implementation(projects.features.lockscreen.api)
    implementation(projects.libraries.core)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.uiStrings)
    implementation(projects.libraries.permissions.api)
    implementation(projects.libraries.qrcode)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.webkit)
    api(projects.features.linknewdevice.api)

    testCommonDependencies(libs, true)
    testImplementation(projects.features.lockscreen.test)
    testImplementation(projects.features.enterprise.test)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.permissions.test)
}
