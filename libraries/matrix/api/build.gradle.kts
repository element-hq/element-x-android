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
    id("io.element.android-library")
    id("kotlin-parcelize")
    alias(libs.plugins.anvil)
    kotlin("plugin.serialization") version "1.8.22"
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
    implementation(projects.libraries.di)
    implementation(libs.dagger)
    implementation(projects.libraries.core)
    implementation("net.java.dev.jna:jna:5.13.0@aar")
    implementation(libs.serialization.json)
    api(projects.libraries.sessionStorage.api)
    implementation(libs.coroutines.core)

    testImplementation(libs.test.junit)
    testImplementation(libs.test.truth)
}
