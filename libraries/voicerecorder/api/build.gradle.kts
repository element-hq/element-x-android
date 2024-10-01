import extension.setupAnvil

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */
plugins {
    id("io.element.android-compose-library")
}

android {
    namespace = "io.element.android.libraries.voicerecorder.api"
}

setupAnvil()

dependencies {
    implementation(libs.androidx.annotationjvm)
    implementation(libs.coroutines.core)
}
