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
    alias(libs.plugins.ksp)
}

android {
    namespace = "io.element.android.features.location.impl"
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

anvil {
    useKsp(
        contributesAndFactoryGeneration = true,
        componentMerging = true,
    )
    generateDaggerFactories = true
    disableComponentMerging = true
}

dependencies {
    api(projects.features.location.api)
    implementation(projects.features.messages.api)
    implementation(projects.libraries.maplibreCompose)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.di)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.core)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.androidutils)
    implementation(projects.services.analytics.api)
    implementation(libs.accompanist.permission)
    implementation(projects.libraries.uiStrings)
    implementation(libs.dagger)
    implementation(projects.anvilannotations)
    ksp(projects.anvilcodegen)
//    ksp(libs.dagger.compiler)

    testImplementation(libs.test.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.molecule.runtime)
    testImplementation(libs.test.robolectric)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.testtags)
    testImplementation(projects.services.analytics.test)
    testImplementation(projects.features.messages.test)
    testImplementation(projects.tests.testutils)
    testImplementation(libs.androidx.compose.ui.test.junit)
    testReleaseImplementation(libs.androidx.compose.ui.test.manifest)
}
