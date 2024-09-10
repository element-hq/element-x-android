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
    namespace = "io.element.android.libraries.mediaupload.impl"

    anvil {
        generateDaggerFactories.set(true)
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    dependencies {
        implementation(projects.anvilannotations)
        anvil(projects.anvilcodegen)

        api(projects.libraries.mediaupload.api)
        implementation(projects.libraries.architecture)
        implementation(projects.libraries.androidutils)
        implementation(projects.libraries.core)
        implementation(projects.libraries.di)
        implementation(projects.libraries.matrix.api)
        implementation(projects.services.toolbox.api)
        implementation(libs.inject)
        implementation(libs.androidx.exifinterface)
        implementation(libs.coroutines.core)
        implementation(libs.otaliastudios.transcoder)
        implementation(libs.vanniktech.blurhash)

        testImplementation(libs.test.junit)
        testImplementation(libs.test.robolectric)
        testImplementation(libs.coroutines.test)
        testImplementation(libs.test.truth)
        testImplementation(projects.tests.testutils)
        testImplementation(projects.services.toolbox.test)
    }
}
