import config.BuildTimeConfig
import extension.buildConfigFieldStr
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
    namespace = "io.element.android.services.analyticsproviders.posthog"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigFieldStr(
            name = "POSTHOG_HOST",
            value = BuildTimeConfig.SERVICES_POSTHOG_HOST.takeIf { isEnterpriseBuild } ?: ""
        )
        buildConfigFieldStr(
            name = "POSTHOG_APIKEY",
            value = BuildTimeConfig.SERVICES_POSTHOG_APIKEY.takeIf { isEnterpriseBuild } ?: ""
        )
    }
}

setupAnvil()

dependencies {
    implementation(libs.dagger)
    implementation(libs.posthog) {
        exclude("com.android.support", "support-annotations")
    }
    implementation(projects.features.enterprise.api)
    implementation(projects.libraries.core)
    implementation(projects.libraries.di)
    implementation(projects.services.analyticsproviders.api)

    testImplementation(libs.coroutines.test)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.junit)
    testImplementation(projects.tests.testutils)
    testImplementation(libs.test.mockk)
}
