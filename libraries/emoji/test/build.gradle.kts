/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.libraries.emoji.test"
}

dependencies {
    api(projects.libraries.emoji.api)
    api(projects.libraries.matrix.api)
    api(libs.coroutines.core)

    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.coroutines.test)
    implementation(libs.matrix.emojibase.bindings)
    implementation(projects.tests.testutils)
}
