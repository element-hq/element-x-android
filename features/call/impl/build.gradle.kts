import extension.readLocalProperty
import extension.setupAnvil

/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "io.element.android.features.call.impl"

    buildFeatures {
        buildConfig = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    defaultConfig {
        buildConfigField(
            type = "String",
            name = "SENTRY_DSN",
            value = (System.getenv("ELEMENT_CALL_SENTRY_DSN")
                ?: readLocalProperty("features.call.sentry.dsn")
                ?: ""
                ).let { "\"$it\"" }
        )
        buildConfigField(
            type = "String",
            name = "POSTHOG_USER_ID",
            value = (System.getenv("ELEMENT_CALL_POSTHOG_USER_ID")
                ?: readLocalProperty("features.call.posthog.userid")
                ?: ""
                ).let { "\"$it\"" }
        )
        buildConfigField(
            type = "String",
            name = "POSTHOG_API_HOST",
            value = (System.getenv("ELEMENT_CALL_POSTHOG_API_HOST")
                ?: readLocalProperty("features.call.posthog.api.host")
                ?: ""
                ).let { "\"$it\"" }
        )
        buildConfigField(
            type = "String",
            name = "POSTHOG_API_KEY",
            value = (System.getenv("ELEMENT_CALL_POSTHOG_API_KEY")
                ?: readLocalProperty("features.call.posthog.api.key")
                ?: ""
                ).let { "\"$it\"" }
        )
        buildConfigField(
            type = "String",
            name = "RAGESHAKE_URL",
            value = (System.getenv("ELEMENT_CALL_RAGESHAKE_URL")
                ?: readLocalProperty("features.call.regeshake.url")
                ?: ""
                ).let { "\"$it\"" }
        )
    }
}

setupAnvil()

dependencies {
    implementation(projects.appconfig)
    implementation(projects.features.enterprise.api)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.core)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.featureflag.api)
    implementation(projects.libraries.matrix.impl)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.network)
    implementation(projects.libraries.preferences.api)
    implementation(projects.libraries.push.api)
    implementation(projects.libraries.uiStrings)
    implementation(projects.services.analytics.api)
    implementation(projects.services.appnavstate.api)
    implementation(projects.services.toolbox.api)
    implementation(libs.androidx.webkit)
    implementation(libs.coil.compose)
    implementation(libs.network.retrofit)
    implementation(libs.serialization.json)
    implementation(libs.element.call.embedded)
    api(projects.features.call.api)

    testImplementation(libs.coroutines.test)
    testImplementation(libs.molecule.runtime)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(libs.test.robolectric)
    testImplementation(libs.test.mockk)
    testImplementation(projects.features.call.test)
    testImplementation(projects.libraries.featureflag.test)
    testImplementation(projects.libraries.preferences.test)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.push.test)
    testImplementation(projects.services.analytics.test)
    testImplementation(projects.services.appnavstate.test)
    testImplementation(projects.tests.testutils)
    testImplementation(libs.androidx.compose.ui.test.junit)
    testReleaseImplementation(libs.androidx.compose.ui.test.manifest)
}
