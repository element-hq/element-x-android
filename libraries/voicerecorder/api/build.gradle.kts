/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */
plugins {
    id("io.element.android-compose-library")
    alias(libs.plugins.anvil)
}

android {
    namespace = "io.element.android.libraries.voicerecorder.api"
}

anvil {
    generateDaggerFactories.set(true)
}

dependencies {
    implementation(libs.androidx.annotationjvm)
    implementation(libs.coroutines.core)
}
