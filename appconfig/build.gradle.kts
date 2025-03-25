import config.BuildTimeConfig
import extension.buildConfigFieldBoolean
import extension.buildConfigFieldStr

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
    namespace = "io.element.android.appconfig"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigFieldStr(
            name = "URL_POLICY",
            value = BuildTimeConfig.URL_POLICY ?: "https://element.io/cookie-policy",
        )
        buildConfigFieldBoolean(
            name = "SERVICES_RAGESHAKE_IS_ENABLED",
            value = BuildTimeConfig.SERVICES_RAGESHAKE_IS_ENABLED,
        )
    }
}

dependencies {
    implementation(libs.androidx.annotationjvm)
    implementation(projects.libraries.matrix.api)
}
