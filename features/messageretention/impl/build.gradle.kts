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
    namespace = "io.element.android.features.messageretention.impl"
    testOptions.unitTests.isIncludeAndroidResources = true
}

setupDependencyInjection()

dependencies {
    api(projects.features.messageretention.api)
    implementation(projects.libraries.architecture)
    testCommonDependencies(libs, true)
    implementation(projects.libraries.di)
}
