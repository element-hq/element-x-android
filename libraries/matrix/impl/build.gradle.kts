import extension.setupDependencyInjection
import extension.testCommonDependencies

/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "io.element.android.libraries.matrix.impl"
}

setupDependencyInjection()

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
    implementation("net.java.dev.jna:jna:5.17.0@aar")
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.serialization.json)
    implementation(libs.kotlinx.collections.immutable)

    testCommonDependencies(libs)
    testImplementation(projects.libraries.featureflag.test)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.preferences.test)
    testImplementation(projects.libraries.previewutils)
    testImplementation(projects.libraries.sessionStorage.test)
    testImplementation(projects.services.analytics.test)
    testImplementation(projects.services.toolbox.test)
}
