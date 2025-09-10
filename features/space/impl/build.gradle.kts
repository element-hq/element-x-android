import extension.ComponentMergingStrategy
import extension.setupAnvil

/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.features.space.impl"

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

setupAnvil(componentMergingStrategy = ComponentMergingStrategy.KSP)

dependencies {
    implementation(projects.libraries.core)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.uiStrings)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.deeplink.api)
    implementation(projects.services.analytics.api)
    implementation(libs.coil.compose)
    implementation(projects.libraries.featureflag.api)
    implementation(projects.features.invite.api)
    implementation(projects.libraries.previewutils)
    api(projects.features.space.api)

    testImplementation(libs.test.junit)
    testImplementation(libs.test.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.molecule.runtime)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(libs.test.robolectric)
    testImplementation(projects.services.analytics.test)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.featureflag.test)
    testImplementation(projects.tests.testutils)
    testImplementation(libs.androidx.compose.ui.test.junit)
    testImplementation(projects.features.invite.test)
    testReleaseImplementation(libs.androidx.compose.ui.test.manifest)
}
