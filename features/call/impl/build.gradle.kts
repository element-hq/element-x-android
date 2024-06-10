/*
 * Copyright (c) 2024 New Vector Ltd
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
    id("io.element.android-compose-library")
    alias(libs.plugins.anvil)
    alias(libs.plugins.ksp)
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
}

anvil {
    generateDaggerFactories.set(true)
}

dependencies {
    implementation(projects.appconfig)
    implementation(projects.anvilannotations)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.core)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.matrix.impl)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.network)
    implementation(projects.libraries.preferences.api)
    implementation(projects.libraries.push.api)
    implementation(projects.libraries.uiStrings)
    implementation(projects.services.analytics.api)
    implementation(projects.services.toolbox.api)
    implementation(libs.androidx.webkit)
    implementation(libs.coil.compose)
    implementation(libs.serialization.json)
    api(projects.features.call.api)
    ksp(libs.showkase.processor)

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
    testImplementation(projects.tests.testutils)
}
