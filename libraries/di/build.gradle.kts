/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("com.android.lint")
}

dependencies {
    api(libs.metro.runtime)
}
