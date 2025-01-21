import extension.setupAnvil

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
}

android {
    namespace = "io.element.android.libraries.eventformatter.impl"

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

setupAnvil()

dependencies {
    implementation(projects.libraries.core)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.uiStrings)
    implementation(projects.services.toolbox.api)
    api(projects.libraries.eventformatter.api)

    testImplementation(projects.services.toolbox.impl)
    testImplementation(libs.test.junit)
    testImplementation(libs.test.robolectric)
    testImplementation(libs.test.truth)
    testImplementation(projects.libraries.matrix.test)
}
