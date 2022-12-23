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
    alias(libs.plugins.ksp)
    // TODO Create alias
    id("app.cash.paparazzi") version "1.0.0"
}

android {
    namespace = "io.element.android.x.tests.uitests"
}

dependencies {
    testImplementation(libs.test.junit)
    androidTestImplementation(libs.test.junitext)
    ksp(libs.showkase.processor)
    kspTest(libs.showkase.processor)

    // TODO Move to libs
    testImplementation("com.airbnb.android:showkase-screenshot-testing:$1.0.0-beta14")
    testImplementation("com.google.testparameterinjector:test-parameter-injector:1.8")

    implementation(project(":libraries:designsystem"))

    implementation(libs.showkase)
    ksp(libs.showkase.processor)
}
