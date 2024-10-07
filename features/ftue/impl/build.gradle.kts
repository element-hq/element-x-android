import extension.setupAnvil

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.features.ftue.impl"
}

setupAnvil()

dependencies {
    api(projects.features.ftue.api)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.core)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.preferences.api)
    implementation(projects.libraries.uiStrings)
    implementation(projects.libraries.testtags)
    implementation(projects.features.analytics.api)
    implementation(projects.features.securebackup.api)
    implementation(projects.features.verifysession.api)
    implementation(projects.services.analytics.api)
    implementation(projects.features.lockscreen.api)
    implementation(projects.libraries.permissions.api)
    implementation(projects.libraries.permissions.noop)
    implementation(projects.services.toolbox.api)

    testImplementation(libs.test.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.molecule.runtime)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.services.analytics.test)
    testImplementation(projects.libraries.permissions.impl)
    testImplementation(projects.libraries.permissions.test)
    testImplementation(projects.libraries.preferences.test)
    testImplementation(projects.features.lockscreen.test)
    testImplementation(projects.services.toolbox.test)
    testImplementation(projects.tests.testutils)
}
