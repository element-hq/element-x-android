import config.BuildTimeConfig
import extension.buildConfigFieldStr
import extension.readLocalProperty
import extension.setupDependencyInjection
import extension.testCommonDependencies

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
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
        buildConfigFieldStr(
            name = "SDK_SENTRY_DSN",
            value = if (isEnterpriseBuild) {
                BuildTimeConfig.SERVICES_SENTRY_DSN_RUST
            } else {
                System.getenv("ELEMENT_SDK_SENTRY_DSN")
                    ?: readLocalProperty("services.analyticsproviders.sdk.sentry.dsn")
            }
                ?: ""
        )
    }
}

setupDependencyInjection()

dependencies {
    implementation(libs.sentry)
    implementation(projects.libraries.core)
    implementation(projects.libraries.di)
    implementation(projects.libraries.matrix.api)
    implementation(projects.services.analyticsproviders.api)
    implementation(projects.services.appnavstate.api)

    testCommonDependencies(libs, false)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.services.appnavstate.test)
}
