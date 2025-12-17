/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */
plugins {
    id("io.element.android-compose-library")
}

android {
    namespace = "io.element.android.services.analytics.api"
}

dependencies {
    api(projects.services.analyticsproviders.api)
    api(projects.services.toolbox.api)
    implementation(libs.coroutines.core)
    implementation(projects.libraries.core)
}
