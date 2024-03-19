/*
 * Copyright (c) 2022 New Vector Ltd
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
    id("kotlin-parcelize")
    alias(libs.plugins.anvil)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "io.element.android.libraries.matrix.api"

    buildFeatures {
        buildConfig = true
    }
}

anvil {
    generateDaggerFactories.set(true)
}

dependencies {
    implementation(projects.appconfig)
    implementation(projects.libraries.di)
    implementation(libs.dagger)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.core)
    implementation(libs.serialization.json)
    api(projects.libraries.sessionStorage.api)
    implementation(libs.coroutines.core)

    testImplementation(libs.test.junit)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.robolectric)
    testImplementation(projects.tests.testutils)
    testImplementation(projects.libraries.matrix.test)
}
