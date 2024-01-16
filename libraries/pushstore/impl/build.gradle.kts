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
}

android {
    namespace = "io.element.android.libraries.push.pushstore.impl"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }
}

anvil {
    generateDaggerFactories.set(true)
}

dependencies {
    implementation(libs.dagger)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.core)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.pushstore.api)
    implementation(projects.libraries.sessionStorage.api)
    implementation(libs.androidx.corektx)
    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.test.junit)
    testImplementation(libs.test.mockk)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(libs.coroutines.test)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.services.appnavstate.test)
    testImplementation(projects.libraries.sessionStorage.test)

    androidTestImplementation(libs.coroutines.test)
    androidTestImplementation(libs.test.core)
    androidTestImplementation(libs.test.junit)
    androidTestImplementation(libs.test.truth)
    androidTestImplementation(libs.test.runner)
    androidTestImplementation(projects.libraries.sessionStorage.test)
}
