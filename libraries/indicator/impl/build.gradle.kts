import extension.setupDependencyInjection
import extension.testCommonDependencies

/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
}

setupDependencyInjection()

android {
    namespace = "io.element.android.libraries.indicator.impl"
}

dependencies {
    implementation(projects.libraries.di)
    implementation(projects.libraries.featureflag.api)
    implementation(projects.libraries.matrix.api)

    implementation(libs.coroutines.core)

    api(projects.libraries.indicator.api)

    testCommonDependencies(libs)
    testImplementation(projects.libraries.featureflag.test)
    testImplementation(projects.libraries.matrix.test)
}
