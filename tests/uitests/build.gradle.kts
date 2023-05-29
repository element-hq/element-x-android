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

import extension.allFeaturesImpl
import extension.allLibrariesImpl
import extension.allServicesImpl

plugins {
    id("io.element.android-compose-library")
    alias(libs.plugins.ksp)
//    alias(libs.plugins.paparazzi)
    id("io.github.takahirom.roborazzi")
}

android {
    namespace = "io.element.android.tests.uitests"

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    testImplementation(libs.test.junit)
    testImplementation(libs.test.parameter.injector)
    testImplementation(projects.libraries.designsystem)
    androidTestImplementation(libs.test.junitext)
    ksp(libs.showkase.processor)
    kspTest(libs.showkase.processor)

    implementation(libs.showkase)

    // Core functions
    testImplementation("io.github.takahirom.roborazzi:roborazzi:1.3.0-alpha-3")
    // JUnit rules
    testImplementation("io.github.takahirom.roborazzi:roborazzi-junit-rule:1.3.0-alpha-3")
    testImplementation("org.robolectric:robolectric:4.10.3")
    testImplementation("org.robolectric:annotations:4.10.3")
    testImplementation("androidx.compose.ui:ui-test-junit4:1.4.3")

    allLibrariesImpl()
    allServicesImpl()
    allFeaturesImpl(rootDir, logger)
}
