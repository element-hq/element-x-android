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
    namespace = "io.element.android.libraries.preferences.impl"
}

setupAnvil()

dependencies {
    api(projects.libraries.preferences.api)
    implementation(libs.dagger)
    implementation(libs.androidx.datastore.preferences)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.di)
    implementation(projects.libraries.core)
    implementation(projects.libraries.matrix.api)
}
