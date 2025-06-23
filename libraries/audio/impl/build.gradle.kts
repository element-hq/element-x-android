import extension.setupAnvil

/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */
plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.libraries.audio.impl"
}

setupAnvil()

dependencies {
    api(projects.libraries.audio.api)

    implementation(libs.androidx.corektx)
    implementation(libs.dagger)
    implementation(projects.libraries.di)
}
