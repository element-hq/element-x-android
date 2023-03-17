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
    kotlin("plugin.serialization") version "1.8.10"
}

android {
    namespace = "io.element.android.libraries.push.impl"
}

anvil {
    generateDaggerFactories.set(true)
}

dependencies {
    implementation(libs.dagger)
    implementation(libs.androidx.corektx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.network.retrofit)
    implementation(libs.serialization.json)

    implementation(projects.libraries.architecture)
    implementation(projects.libraries.uiStrings)
    implementation(projects.libraries.core)
    implementation(projects.libraries.di)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.network)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.push.api)

    implementation(projects.services.analytics.api)
    implementation(projects.services.toolbox.api)

    api("me.gujun.android:span:1.7") {
        exclude(group = "com.android.support", module = "support-annotations")
    }


    implementation(platform(libs.google.firebase.bom))
    implementation("com.google.firebase:firebase-messaging-ktx")

    // UnifiedPush
    api("com.github.UnifiedPush:android-connector:2.1.1")

    testImplementation(libs.test.junit)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(libs.coroutines.test)
}
