/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.libraries.featureflag.test"

    dependencies {
        api(projects.libraries.featureflag.api)
        implementation(projects.libraries.core)
        implementation(projects.libraries.matrix.test)
        implementation(libs.coroutines.core)
    }
}
