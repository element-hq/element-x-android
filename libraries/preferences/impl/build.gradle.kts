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
    namespace = "io.element.android.libraries.preferences.impl"
}

anvil {
    generateDaggerFactories.set(true)
}

dependencies {
    api(projects.libraries.preferences.api)
    implementation(libs.dagger)
    implementation(libs.androidx.datastore.preferences)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.di)
    implementation(projects.libraries.core)
    implementation(projects.libraries.matrix.api)
}
