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
    alias(libs.plugins.ksp)
}

android {
    namespace = "io.element.android.libraries.androidutils"

    buildFeatures {
        buildConfig = true
    }
}

anvil {
    useKsp(
        contributesAndFactoryGeneration = true,
        componentMerging = true,
    )
//    generateDaggerFactories = true
}

dependencies {
    ksp(projects.anvilcodegen)
    ksp(libs.dagger.compiler)
    implementation(projects.anvilannotations)
    implementation(projects.libraries.di)

    implementation(projects.libraries.core)
    implementation(projects.services.toolbox.api)
    implementation(libs.dagger)
    implementation(libs.timber)
    implementation(libs.androidx.corektx)
    implementation(libs.androidx.activity.activity)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.security.crypto)
    api(libs.androidx.browser)

    testImplementation(projects.tests.testutils)
    testImplementation(libs.test.junit)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.robolectric)
    testImplementation(libs.coroutines.core)
    testImplementation(libs.coroutines.test)
    testImplementation(projects.services.toolbox.test)
}
