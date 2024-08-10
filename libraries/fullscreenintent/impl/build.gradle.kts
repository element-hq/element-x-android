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
}

android {
    namespace = "io.element.android.libraries.fullscreenintent.impl"
}

anvil {
    useKsp(
        contributesAndFactoryGeneration = true,
        componentMerging = true,
    )
//    generateDaggerFactories = true
}

dependencies {
    api(projects.libraries.fullscreenintent.api)
    implementation(projects.anvilannotations)
    ksp(projects.anvilcodegen)
    ksp(libs.dagger.compiler)

    implementation(projects.libraries.core)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.permissions.api)
    implementation(projects.libraries.permissions.noop)
    implementation(projects.libraries.preferences.api)
    implementation(projects.services.toolbox.api)
    implementation(libs.androidx.datastore.preferences)

    testImplementation(projects.libraries.fullscreenintent.test)
    testImplementation(libs.test.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.molecule.runtime)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(projects.tests.testutils)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.permissions.test)
    testImplementation(projects.libraries.preferences.test)
    testImplementation(projects.libraries.testtags)
    testImplementation(libs.test.robolectric)
    testImplementation(libs.test.mockk)
    testImplementation(libs.androidx.compose.ui.test.junit)
    testImplementation(projects.services.toolbox.test)
    testReleaseImplementation(libs.androidx.compose.ui.test.manifest)
}
