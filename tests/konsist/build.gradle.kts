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
    id("io.element.android-compose-library")
}

android {
    namespace = "io.element.android.tests.konsist"
}

dependencies {
    val composeBom = platform(libs.androidx.compose.bom)
    testImplementation(composeBom)
    testImplementation("androidx.compose.ui:ui-tooling-preview")
    testImplementation(libs.test.junit)
    testImplementation(libs.test.konsist)
    testImplementation(libs.test.truth)
    testImplementation(projects.libraries.architecture)
    testImplementation(projects.libraries.designsystem)
}

// Make sure Konsist tests are always run. This is needed because otherwise we'd have to either:
// - Add every single module as a dependency of this one.
// - Move the Konsist tests to the `app` module, but the `app` module does not need to know about Konsist.
tasks.withType<Test>().configureEach {
    outputs.upToDateWhen { false }
}
