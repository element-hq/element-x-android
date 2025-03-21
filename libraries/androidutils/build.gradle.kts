import extension.setupAnvil

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */
plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.libraries.androidutils"

    buildFeatures {
        buildConfig = true
    }
}

setupAnvil()

dependencies {
    implementation(projects.libraries.di)

    implementation(projects.libraries.core)
    implementation(projects.services.toolbox.api)
    implementation(libs.dagger)
    implementation(libs.timber)
    implementation(libs.androidx.corektx)
    implementation(libs.androidx.activity.activity)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.exifinterface)
    api(libs.androidx.browser)

    testImplementation(projects.tests.testutils)
    testImplementation(libs.test.junit)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.robolectric)
    testImplementation(libs.coroutines.core)
    testImplementation(libs.coroutines.test)
    testImplementation(projects.services.toolbox.test)
}
