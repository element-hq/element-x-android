import extension.setupDependencyInjection
import extension.testCommonDependencies

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-library")
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "io.element.android.libraries.wellknown.impl"
}

setupDependencyInjection()

dependencies {
    implementation(platform(libs.network.retrofit.bom))
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.architecture)

    testCommonDependencies(libs)
}
