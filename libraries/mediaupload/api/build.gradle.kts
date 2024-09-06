/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

plugins {
    id("io.element.android-library")
    alias(libs.plugins.anvil)
}

android {
    namespace = "io.element.android.libraries.mediaupload.api"

    anvil {
        generateDaggerFactories.set(true)
    }

    dependencies {
        implementation(projects.anvilannotations)
        anvil(projects.anvilcodegen)

        implementation(projects.libraries.architecture)
        implementation(projects.libraries.androidutils)
        implementation(projects.libraries.core)
        implementation(projects.libraries.di)
        api(projects.libraries.matrix.api)
        implementation(libs.inject)
        implementation(libs.coroutines.core)

        testImplementation(projects.libraries.matrix.test)
        testImplementation(projects.libraries.mediaupload.test)
        testImplementation(projects.tests.testutils)
        testImplementation(libs.test.junit)
        testImplementation(libs.test.truth)
        testImplementation(libs.coroutines.test)
        testImplementation(libs.test.robolectric)
    }
}
