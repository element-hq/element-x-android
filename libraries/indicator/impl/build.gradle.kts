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
    alias(libs.plugins.anvil)
}

anvil {
    generateDaggerFactories.set(true)
}

android {
    namespace = "io.element.android.libraries.indicator.impl"
}

dependencies {
    anvil(projects.anvilcodegen)
    implementation(libs.dagger)
    implementation(projects.libraries.di)
    implementation(projects.libraries.featureflag.api)
    implementation(projects.libraries.matrix.api)
    implementation(projects.anvilannotations)

    implementation(libs.coroutines.core)

    api(projects.libraries.indicator.api)

    testImplementation(projects.libraries.featureflag.test)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(libs.test.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.test.turbine)
    testImplementation(libs.test.truth)
}
