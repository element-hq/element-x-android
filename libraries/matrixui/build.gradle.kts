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
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.libraries.matrix.ui"
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
    implementation(projects.anvilannotations)
    ksp(projects.anvilcodegen)
//    ksp(libs.dagger.compiler)
    implementation(projects.libraries.di)

    implementation(projects.libraries.architecture)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.core)
    implementation(projects.libraries.uiStrings)
    implementation(projects.libraries.testtags)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.jsoup)

    testImplementation(libs.coroutines.test)
    testImplementation(libs.test.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.molecule.runtime)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.dateformatter.test)
    testImplementation(projects.tests.testutils)
    testImplementation(libs.test.mockk)
    testImplementation(libs.test.robolectric)
    testImplementation(libs.androidx.compose.ui.test.junit)
    testImplementation(projects.libraries.sessionStorage.test)
}
