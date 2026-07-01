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
}

android {
    namespace = "io.element.android.features.recentcalls.impl"

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

setupDependencyInjection()

dependencies {
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.core)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.dateformatter.api)
    implementation(projects.libraries.uiStrings)
    implementation(projects.features.call.api)
    api(projects.features.recentcalls.api)

    testCommonDependencies(libs, true)
    testImplementation(projects.features.recentcalls.test)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.dateformatter.test)
}
