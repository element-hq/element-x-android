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
    alias(libs.plugins.anvil)
    alias(libs.plugins.ksp)
}

anvil {
    useKsp(
        contributesAndFactoryGeneration = true,
        componentMerging = true,
    )
    generateDaggerFactories = true
    disableComponentMerging = true
}

android {
    namespace = "io.element.android.services.appnavstate.impl"
}

dependencies {
    ksp(projects.anvilcodegen)
//    ksp(libs.dagger.compiler)
    implementation(libs.dagger)
    implementation(projects.libraries.di)

    implementation(projects.libraries.core)
    implementation(projects.libraries.matrix.api)
    implementation(projects.anvilannotations)

    implementation(libs.coroutines.core)
    implementation(libs.androidx.corektx)
    implementation(libs.androidx.lifecycle.process)

    api(projects.services.appnavstate.api)

    testImplementation(libs.test.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.test.truth)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.tests.testutils)
    testImplementation(projects.services.appnavstate.test)
}
