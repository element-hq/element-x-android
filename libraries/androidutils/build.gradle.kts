import extension.setupDependencyInjection
import extension.testCommonDependencies

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

setupDependencyInjection()

dependencies {
    implementation(projects.libraries.di)

    implementation(projects.libraries.core)
    implementation(projects.services.toolbox.api)
    implementation(libs.timber)
    implementation(libs.androidx.corektx)
    implementation(libs.androidx.activity.activity)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.datastore.preferences)
    api(libs.androidx.browser)

    testCommonDependencies(libs)
    testImplementation(libs.coroutines.core)
    testImplementation(projects.services.toolbox.test)
}
