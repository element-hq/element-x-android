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
    id("io.element.android-library")
    alias(libs.plugins.anvil)
    kotlin("plugin.serialization") version "1.8.22"
}

android {
    namespace = "io.element.android.libraries.pushproviders.unifiedpush"
}

anvil {
    generateDaggerFactories.set(true)
}

dependencies {
    implementation(libs.dagger)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.core)
    implementation(projects.libraries.matrix.api)

    implementation(projects.libraries.pushstore.api)
    implementation(projects.libraries.pushproviders.api)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.core)
    implementation(projects.services.toolbox.api)

    implementation(projects.libraries.network)
    implementation(platform(libs.network.okhttp.bom))
    implementation("com.squareup.okhttp3:okhttp")
    implementation(libs.network.retrofit)

    implementation(libs.serialization.json)

    // UnifiedPush library
    api(libs.unifiedpush)

    testImplementation(libs.test.junit)
    testImplementation(libs.test.truth)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.tests.testutils)
}
