import extension.setupAnvil

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */
plugins {
    id("io.element.android-compose-library")
}

android {
    namespace = "io.element.android.services.analytics.noop"
}

setupAnvil()

dependencies {
    implementation(libs.dagger)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.di)
    api(projects.services.analytics.api)
}
