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
    implementation(libs.network.okhttp.okhttp)
    implementation(projects.libraries.core)
    implementation(projects.libraries.di)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.sessionStorage.api)
    implementation(projects.services.appnavstate.api)
    api(projects.features.networkmonitor.api)
}
