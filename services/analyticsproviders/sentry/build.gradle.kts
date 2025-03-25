import config.BuildTimeConfig
import extension.buildConfigFieldStr
import extension.readLocalProperty
import extension.setupAnvil

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */
plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.services.analyticsproviders.sentry"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigFieldStr(
            name = "SENTRY_DSN",
            value = if (isEnterpriseBuild) {
                BuildTimeConfig.SERVICES_SENTRY_DSN
            } else {
                System.getenv("ELEMENT_ANDROID_SENTRY_DSN")
                    ?: readLocalProperty("services.analyticsproviders.sentry.dsn")
            }
                ?: ""
        )
    }
}

setupAnvil()

dependencies {
    implementation(libs.dagger)
    implementation(libs.sentry)
    implementation(projects.libraries.core)
    implementation(projects.libraries.di)
    implementation(projects.services.analyticsproviders.api)
}
