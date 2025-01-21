import extension.setupAnvil

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-library")
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.libraries.featureflag.impl"
}

setupAnvil()

dependencies {
    api(projects.libraries.featureflag.api)
    implementation(libs.dagger)
    implementation(libs.androidx.datastore.preferences)
    implementation(projects.appconfig)
    implementation(projects.libraries.di)
    implementation(projects.libraries.core)
    implementation(libs.coroutines.core)
    testImplementation(libs.test.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(projects.libraries.matrix.test)
}
