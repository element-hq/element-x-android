import extension.setupDependencyInjection
import extension.testCommonDependencies

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-library")
}

setupDependencyInjection()

android {
    namespace = "io.element.android.services.appnavstate.impl"
}

dependencies {
    implementation(projects.libraries.core)
    implementation(projects.libraries.di)
    implementation(projects.libraries.matrix.api)

    implementation(libs.coroutines.core)
    implementation(libs.androidx.corektx)
    implementation(libs.androidx.lifecycle.process)

    api(projects.services.appnavstate.api)

    testCommonDependencies(libs)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.services.appnavstate.test)
}
