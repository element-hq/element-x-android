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
    namespace = "io.element.android.features.preferences.impl"
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
    implementation(projects.anvilannotations)
    anvil(projects.anvilcodegen)
    implementation(projects.libraries.androidutils)
    implementation(projects.appconfig)
    implementation(projects.libraries.core)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.featureflag.api)
    implementation(projects.libraries.featureflag.ui)
    implementation(projects.libraries.network)
    implementation(projects.libraries.pushstore.api)
    implementation(projects.libraries.indicator.api)
    implementation(projects.libraries.preferences.api)
    implementation(projects.libraries.testtags)
    implementation(projects.libraries.uiStrings)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.mediapickers.api)
    implementation(projects.libraries.mediaupload.api)
    implementation(projects.libraries.permissions.api)
    implementation(projects.libraries.push.api)
    implementation(projects.features.rageshake.api)
    implementation(projects.features.lockscreen.api)
    implementation(projects.features.analytics.api)
    implementation(projects.features.ftue.api)
    implementation(projects.features.logout.api)
    implementation(projects.features.roomlist.api)
    implementation(projects.services.analytics.api)
    implementation(projects.services.toolbox.api)
    implementation(libs.datetime)
    implementation(libs.coil.compose)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.datastore.preferences)
    api(projects.features.preferences.api)
    ksp(libs.showkase.processor)

    testImplementation(libs.test.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.molecule.runtime)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(libs.test.mockk)
    testImplementation(libs.test.robolectric)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.featureflag.test)
    testImplementation(projects.libraries.mediapickers.test)
    testImplementation(projects.libraries.mediaupload.test)
    testImplementation(projects.libraries.permissions.test)
    testImplementation(projects.libraries.preferences.test)
    testImplementation(projects.libraries.push.test)
    testImplementation(projects.libraries.pushstore.test)
    testImplementation(projects.features.rageshake.test)
    testImplementation(projects.features.rageshake.impl)
    testImplementation(projects.libraries.indicator.impl)
    testImplementation(projects.features.logout.impl)
    testImplementation(projects.services.analytics.test)
    testImplementation(projects.services.toolbox.test)
    testImplementation(projects.features.analytics.impl)
    testImplementation(projects.tests.testutils)
    testImplementation(libs.androidx.compose.ui.test.junit)
    testReleaseImplementation(libs.androidx.compose.ui.test.manifest)
}
