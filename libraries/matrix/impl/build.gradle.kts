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
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "io.element.android.libraries.matrix.impl"
}

anvil {
    useKsp(
        contributesAndFactoryGeneration = true,
        componentMerging = true,
    )
//    generateDaggerFactories = true
}

dependencies {
    releaseImplementation(libs.matrix.sdk)
    if (file("${rootDir.path}/libraries/rustsdk/matrix-rust-sdk.aar").exists()) {
        println("\nNote: Using local binary of the Rust SDK.\n")
        debugImplementation(projects.libraries.rustsdk)
    } else {
        debugImplementation(libs.matrix.sdk)
    }
    implementation(projects.appconfig)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.di)
    implementation(projects.libraries.featureflag.api)
    implementation(projects.libraries.network)
    implementation(projects.libraries.preferences.api)
    implementation(projects.services.analytics.api)
    implementation(projects.services.toolbox.api)
    api(projects.libraries.matrix.api)
    implementation(projects.libraries.core)
    implementation("net.java.dev.jna:jna:5.14.0@aar")
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.serialization.json)
    implementation(libs.kotlinx.collections.immutable)

    implementation(projects.anvilannotations)
    ksp(projects.anvilcodegen)
    implementation(libs.dagger)
    ksp(libs.dagger.compiler)

    testImplementation(libs.test.junit)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.robolectric)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.services.analytics.test)
    testImplementation(projects.tests.testutils)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.test.turbine)
}
