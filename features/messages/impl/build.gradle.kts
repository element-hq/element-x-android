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
    namespace = "io.element.android.features.messages.impl"
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
    api(projects.features.messages.api)
    implementation(projects.appconfig)
    implementation(projects.features.call.api)
    implementation(projects.features.location.api)
    implementation(projects.features.poll.api)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.core)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.textcomposer.impl)
    implementation(projects.libraries.uiStrings)
    implementation(projects.libraries.dateformatter.api)
    implementation(projects.libraries.eventformatter.api)
    implementation(projects.libraries.mediapickers.api)
    implementation(projects.libraries.mediaviewer.api)
    implementation(projects.libraries.featureflag.api)
    implementation(projects.libraries.mediaupload.api)
    implementation(projects.libraries.permissions.api)
    implementation(projects.libraries.preferences.api)
    implementation(projects.libraries.roomselect.api)
    implementation(projects.libraries.voicerecorder.api)
    implementation(projects.libraries.mediaplayer.api)
    implementation(projects.libraries.uiUtils)
    implementation(projects.libraries.testtags)
    implementation(projects.features.networkmonitor.api)
    implementation(projects.services.analytics.api)
    implementation(projects.services.toolbox.api)
    implementation(libs.coil.compose)
    implementation(libs.datetime)
    implementation(libs.jsoup)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.vanniktech.blurhash)
    implementation(libs.telephoto.zoomableimage)
    implementation(libs.matrix.emojibase.bindings)

    testImplementation(libs.test.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.molecule.runtime)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.dateformatter.test)
    testImplementation(projects.features.networkmonitor.test)
    testImplementation(projects.features.messages.test)
    testImplementation(projects.services.analytics.test)
    testImplementation(projects.services.toolbox.test)
    testImplementation(projects.tests.testutils)
    testImplementation(projects.libraries.featureflag.test)
    testImplementation(projects.libraries.mediaupload.test)
    testImplementation(projects.libraries.mediapickers.test)
    testImplementation(projects.libraries.permissions.test)
    testImplementation(projects.libraries.preferences.test)
    testImplementation(projects.libraries.voicerecorder.test)
    testImplementation(projects.libraries.mediaplayer.test)
    testImplementation(projects.libraries.mediaviewer.test)
    testImplementation(projects.libraries.testtags)
    testImplementation(libs.test.mockk)
    testImplementation(libs.test.robolectric)
    testImplementation(projects.features.poll.test)
    testImplementation(projects.features.poll.impl)
    testImplementation(libs.androidx.compose.ui.test.junit)
    testReleaseImplementation(libs.androidx.compose.ui.test.manifest)

    ksp(libs.showkase.processor)
}
