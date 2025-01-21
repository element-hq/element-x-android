/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.services.appnavstate.test"
}

dependencies {
    api(projects.libraries.matrix.api)
    api(projects.services.appnavstate.api)
    implementation(libs.coroutines.core)
    implementation(libs.androidx.lifecycle.runtime)
}
