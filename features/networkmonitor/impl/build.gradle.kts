import extension.setupDependencyInjection

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
    namespace = "io.element.android.features.networkmonitor.impl"
}

dependencies {
    implementation(libs.coroutines.core)
    implementation(projects.libraries.core)
    implementation(projects.libraries.di)
    api(projects.features.networkmonitor.api)
}
