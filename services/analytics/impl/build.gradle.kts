import extension.setupAnvil

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.services.analytics.impl"
}

setupAnvil()

dependencies {
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
