import extension.setupAnvil

/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
}

setupAnvil()

android {
    namespace = "io.element.android.libraries.dateformatter.impl"

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    dependencies {
        implementation(libs.dagger)
        implementation(projects.libraries.core)
        implementation(projects.libraries.designsystem)
        implementation(projects.libraries.di)
        implementation(projects.libraries.uiStrings)
        implementation(projects.services.toolbox.api)

        api(projects.libraries.dateformatter.api)
        api(libs.datetime)

        testImplementation(libs.test.junit)
        testImplementation(libs.test.truth)
        testImplementation(libs.test.turbine)
        testImplementation(libs.test.robolectric)
        testImplementation(projects.libraries.dateformatter.test)
        testImplementation(projects.services.toolbox.test)
        testImplementation(projects.tests.testutils)
        testImplementation(libs.androidx.compose.ui.test.junit)
    }
}
