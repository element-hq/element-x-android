import extension.setupAnvil

/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

plugins {
    id("io.element.android-library")
}

setupAnvil()

android {
    namespace = "io.element.android.features.networkmonitor.impl"
}

dependencies {
    implementation(libs.coroutines.core)
    implementation(libs.dagger)
    implementation(projects.libraries.core)
    implementation(projects.libraries.di)
    api(projects.features.networkmonitor.api)
}
