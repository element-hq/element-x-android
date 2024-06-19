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
}

android {
    namespace = "io.element.android.libraries.push.test"
}

dependencies {
    api(projects.libraries.push.api)
    implementation(projects.libraries.push.impl)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.pushproviders.api)
    implementation(projects.tests.testutils)
    implementation(libs.androidx.core)
    implementation(libs.coil.compose)
    implementation(libs.coil.test)
    implementation(libs.test.robolectric)
}
