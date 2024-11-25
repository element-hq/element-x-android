import extension.setupAnvil

/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.features.ftue.test"
}

setupAnvil()

dependencies {
    implementation(projects.features.ftue.api)
    implementation(projects.tests.testutils)
}
