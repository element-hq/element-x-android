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
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "io.element.android.features.login.impl"

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

setupAnvil(componentMergingStrategy = ComponentMergingStrategy.KSP)

dependencies {
    implementation(projects.appconfig)
    implementation(projects.features.enterprise.api)
    implementation(projects.features.rageshake.api)
    implementation(projects.libraries.core)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.featureflag.api)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.network)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.testtags)
    implementation(projects.libraries.uiStrings)
    implementation(projects.libraries.permissions.api)
    implementation(projects.libraries.qrcode)
    implementation(projects.libraries.oidc.api)
    implementation(libs.androidx.browser)
    implementation(platform(libs.network.retrofit.bom))
    implementation(libs.androidx.webkit)
    implementation(libs.network.retrofit)
    implementation(libs.serialization.json)
    api(projects.features.login.api)

    testImplementation(libs.test.junit)
    testImplementation(libs.androidx.compose.ui.test.junit)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.molecule.runtime)
    testImplementation(libs.test.robolectric)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(projects.features.login.test)
    testImplementation(projects.features.enterprise.test)
    testImplementation(projects.libraries.featureflag.test)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.oidc.test)
    testImplementation(projects.libraries.permissions.test)
    testImplementation(projects.tests.testutils)
    testReleaseImplementation(libs.androidx.compose.ui.test.manifest)
}
