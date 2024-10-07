/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

@file:Suppress("UnstableApiUsage")

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
                value = if (isEnterpriseBuild) {
                    "1:912726360885:android:d273c2077ec3291500427c"
                } else {
                    "1:912726360885:android:d097de99a4c23d2700427c"
                }
            )
        }
        getByName("debug") {
            resValue(
                type = "string",
                name = "google_app_id",
                value = if (isEnterpriseBuild) {
                    "1:912726360885:android:f8de9126a94143d300427c"
                } else {
                    "1:912726360885:android:def0a4e454042e9b00427c"
                }
            )
        }
        register("nightly") {
            consumerProguardFiles("consumer-proguard-rules.pro")
            matchingFallbacks += listOf("release")
            resValue(
                type = "string",
                name = "google_app_id",
                value = if (isEnterpriseBuild) {
                    "1:912726360885:android:3f7e1fe644d99d5a00427c"
                } else {
                    "1:912726360885:android:e17435e0beb0303000427c"
                }
            )
        }
    }
}

setupAnvil()

dependencies {
    implementation(libs.dagger)
    implementation(libs.androidx.corektx)
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
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.push.test)
    testImplementation(projects.libraries.pushstore.test)
    testImplementation(projects.libraries.sessionStorage.implMemory)
    testImplementation(projects.libraries.sessionStorage.test)
    testImplementation(projects.tests.testutils)
    testImplementation(projects.services.toolbox.test)
}
