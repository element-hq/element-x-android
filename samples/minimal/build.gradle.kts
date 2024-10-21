/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */
plugins {
    id("io.element.android-compose-application")
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "io.element.android.samples.minimal"

    defaultConfig {
        applicationId = "io.element.android.samples.minimal"
        targetSdk = Versions.targetSdk
        versionCode = Versions.versionCode
        versionName = Versions.versionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.preference)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.matrix.impl)
    implementation(projects.libraries.permissions.noop)
    implementation(projects.libraries.sessionStorage.implMemory)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.core)
    implementation(projects.libraries.network)
    implementation(projects.libraries.dateformatter.impl)
    implementation(projects.libraries.eventformatter.impl)
    implementation(projects.libraries.fullscreenintent.impl)
    implementation(projects.libraries.preferences.impl)
    implementation(projects.libraries.preferences.test)
    implementation(projects.libraries.indicator.impl)
    implementation(projects.features.invite.impl)
    implementation(projects.features.roomlist.impl)
    implementation(projects.features.leaveroom.impl)
    implementation(projects.features.login.impl)
    implementation(projects.features.logout.impl)
    implementation(projects.features.networkmonitor.impl)
    implementation(projects.services.toolbox.impl)
    implementation(projects.libraries.featureflag.impl)
    implementation(projects.services.analytics.noop)
    implementation(libs.coroutines.core)
    implementation(projects.libraries.push.test)
}
