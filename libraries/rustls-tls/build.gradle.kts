import extension.buildConfigFieldBoolean

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
    namespace = "org.rustls.platformverifier"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigFieldBoolean("TEST", false)
    }
}
