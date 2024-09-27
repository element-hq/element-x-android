import extension.setupAnvil

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
}

android {
    namespace = "io.element.android.features.cachecleaner.impl"
}

setupAnvil()

dependencies {
    api(projects.features.cachecleaner.api)
    implementation(projects.libraries.core)
    implementation(projects.libraries.architecture)

    testImplementation(libs.test.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.test.truth)
    testImplementation(projects.tests.testutils)
}
