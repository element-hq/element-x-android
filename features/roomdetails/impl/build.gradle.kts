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
    alias(libs.plugins.anvil)
    alias(libs.plugins.ksp)
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.features.roomdetails.impl"
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

anvil {
    generateDaggerFactories.set(true)
}

dependencies {
    anvil(projects.anvilcodegen)
    implementation(projects.anvilannotations)

    implementation(projects.libraries.core)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.uiStrings)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.mediapickers.api)
    implementation(projects.libraries.mediaupload.api)
    implementation(projects.libraries.mediaviewer.api)
    implementation(projects.libraries.featureflag.api)
    implementation(projects.libraries.permissions.api)
    implementation(projects.libraries.preferences.api)
    implementation(projects.libraries.testtags)
    api(projects.features.roomdetails.api)
    api(projects.libraries.usersearch.api)
    api(projects.services.apperror.api)
    implementation(libs.coil.compose)
    implementation(projects.features.leaveroom.api)
    implementation(projects.features.createroom.api)
    implementation(projects.features.userprofile.shared)
    implementation(projects.services.analytics.api)
    implementation(projects.features.poll.api)

    testImplementation(libs.test.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.molecule.runtime)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(libs.test.mockk)
    testImplementation(libs.test.robolectric)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.mediaupload.test)
    testImplementation(projects.libraries.mediapickers.test)
    testImplementation(projects.libraries.permissions.test)
    testImplementation(projects.libraries.usersearch.test)
    testImplementation(projects.libraries.featureflag.test)
    testImplementation(projects.tests.testutils)
    testImplementation(projects.features.leaveroom.test)
    testImplementation(projects.features.createroom.test)
    testImplementation(projects.services.analytics.test)
    testImplementation(libs.androidx.compose.ui.test.junit)
    testReleaseImplementation(libs.androidx.compose.ui.test.manifest)

    ksp(libs.showkase.processor)
}
