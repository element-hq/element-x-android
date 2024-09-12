/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

@file:Suppress("UnstableApiUsage")

import extension.allFeaturesApi

plugins {
    id("io.element.android-compose-library")
    alias(libs.plugins.anvil)
    alias(libs.plugins.kapt)
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.appnav"
}

dependencies {
    implementation(projects.anvilannotations)
    anvil(projects.anvilcodegen)
    implementation(libs.dagger)
    kapt(libs.dagger.compiler)

    allFeaturesApi(rootDir, logger)

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
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.oidc.impl)
    testImplementation(projects.libraries.push.test)
    testImplementation(projects.libraries.pushproviders.test)
    testImplementation(projects.features.networkmonitor.test)
    testImplementation(projects.features.login.impl)
    testImplementation(projects.tests.testutils)
    testImplementation(projects.features.rageshake.test)
    testImplementation(projects.features.rageshake.impl)
    testImplementation(projects.features.share.test)
    testImplementation(projects.services.appnavstate.test)
    testImplementation(projects.services.analytics.test)
    testImplementation(libs.test.appyx.junit)
    testImplementation(libs.test.arch.core)
}
