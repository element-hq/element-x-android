
/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.core)
    implementation(projects.libraries.network)
    implementation(projects.libraries.dateformatter.impl)
    implementation(projects.libraries.eventformatter.impl)
    implementation(projects.libraries.fullscreenintent.impl)
    implementation(projects.libraries.preferences.impl)
    implementation(projects.libraries.indicator.impl)
    implementation(projects.features.invite.impl)
    implementation(projects.features.roomlist.impl)
    implementation(projects.features.leaveroom.impl)
    implementation(projects.features.login.impl)
    implementation(projects.features.networkmonitor.impl)
    implementation(projects.services.toolbox.impl)
    implementation(projects.libraries.featureflag.impl)
    implementation(projects.services.analytics.noop)
    implementation(libs.coroutines.core)
    implementation(projects.libraries.push.test)
}
