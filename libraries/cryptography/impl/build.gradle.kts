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
    namespace = "io.element.android.libraries.cryptography.impl"
}

setupAnvil()

dependencies {
    implementation(libs.dagger)
    implementation(projects.libraries.di)
    api(projects.libraries.cryptography.api)

    testImplementation(libs.test.junit)
    testImplementation(libs.test.truth)
}
