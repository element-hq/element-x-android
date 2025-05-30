/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

@file:Suppress("UnstableApiUsage")

import extension.allFeaturesApi
import extension.setupAnvil

plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.appnav"
}

setupAnvil()

dependencies {
    allFeaturesApi(project)

    implementation(projects.libraries.core)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.deeplink)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.oidc.api)
    implementation(projects.libraries.preferences.api)
    implementation(projects.libraries.push.api)
    implementation(projects.libraries.pushproviders.api)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.uiStrings)

    implementation(libs.coil)

    implementation(projects.features.ftue.api)
    implementation(projects.features.share.api)
    implementation(projects.features.viewfolder.api)

    implementation(projects.services.apperror.impl)
    implementation(projects.services.appnavstate.api)
    implementation(projects.services.analytics.api)

    testImplementation(libs.test.junit)
    testImplementation(libs.test.robolectric)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.molecule.runtime)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(projects.features.login.test)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.oidc.test)
    testImplementation(projects.libraries.preferences.test)
    testImplementation(projects.libraries.push.test)
    testImplementation(projects.libraries.pushproviders.test)
    testImplementation(projects.features.networkmonitor.test)
    testImplementation(projects.tests.testutils)
    testImplementation(projects.features.rageshake.test)
    testImplementation(projects.features.share.test)
    testImplementation(projects.services.appnavstate.test)
    testImplementation(projects.services.analytics.test)
    testImplementation(libs.test.appyx.junit)
    testImplementation(libs.test.arch.core)
}
