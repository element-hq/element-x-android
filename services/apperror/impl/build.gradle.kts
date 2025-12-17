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
    id("io.element.android-compose-library")
}

setupDependencyInjection()

android {
    namespace = "io.element.android.services.apperror.impl"
}

dependencies {
    implementation(projects.libraries.core)
    implementation(projects.libraries.di)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.uiStrings)
    implementation(projects.services.toolbox.api)

    implementation(libs.coroutines.core)
    implementation(libs.androidx.corektx)

    api(projects.services.apperror.api)

    testCommonDependencies(libs)
    testImplementation(projects.services.toolbox.test)
}
