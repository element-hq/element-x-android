/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
    alias(libs.plugins.anvil)
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.services.analytics.impl"
}

anvil {
    generateDaggerFactories.set(true)
}

dependencies {
    anvil(projects.anvilcodegen)

    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.core)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.sessionStorage.api)

    api(projects.services.analyticsproviders.api)
    api(projects.services.analytics.api)
    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.coroutines.test)
    testImplementation(libs.test.junit)
    testImplementation(libs.test.truth)
    testImplementation(projects.libraries.sessionStorage.test)
    testImplementation(projects.services.analyticsproviders.test)
    testImplementation(projects.tests.testutils)
}
