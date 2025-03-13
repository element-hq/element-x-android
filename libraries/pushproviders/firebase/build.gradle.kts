/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

@file:Suppress("UnstableApiUsage")

import config.BuildTimeConfig
import extension.setupAnvil

plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.libraries.pushproviders.firebase"

    buildTypes {
        getByName("release") {
            consumerProguardFiles("consumer-proguard-rules.pro")
            resValue(
                type = "string",
                name = "google_app_id",
                value = BuildTimeConfig.GOOGLE_APP_ID_RELEASE,
            )
        }
        getByName("debug") {
            resValue(
                type = "string",
                name = "google_app_id",
                value = BuildTimeConfig.GOOGLE_APP_ID_DEBUG,
            )
        }
        register("nightly") {
            consumerProguardFiles("consumer-proguard-rules.pro")
            matchingFallbacks += listOf("release")
            resValue(
                type = "string",
                name = "google_app_id",
                value = BuildTimeConfig.GOOGLE_APP_ID_NIGHTLY,
            )
        }
    }
}

setupAnvil()

dependencies {
    implementation(libs.dagger)
    implementation(libs.androidx.corektx)
    implementation(projects.features.enterprise.api)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.core)
    implementation(projects.libraries.di)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.uiStrings)
    implementation(projects.libraries.troubleshoot.api)
    implementation(projects.services.toolbox.api)

    implementation(projects.libraries.pushstore.api)
    implementation(projects.libraries.pushproviders.api)

    api(platform(libs.google.firebase.bom))
    api("com.google.firebase:firebase-messaging-ktx") {
        exclude(group = "com.google.firebase", module = "firebase-core")
        exclude(group = "com.google.firebase", module = "firebase-analytics")
        exclude(group = "com.google.firebase", module = "firebase-measurement-connector")
    }

    testImplementation(libs.coroutines.test)
    testImplementation(libs.test.junit)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(libs.test.robolectric)
    testImplementation(libs.kotlinx.collections.immutable)
    testImplementation(projects.features.enterprise.test)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.push.test)
    testImplementation(projects.libraries.pushstore.test)
    testImplementation(projects.libraries.sessionStorage.implMemory)
    testImplementation(projects.libraries.sessionStorage.test)
    testImplementation(projects.tests.testutils)
    testImplementation(projects.services.toolbox.test)
}
