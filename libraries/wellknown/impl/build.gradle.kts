import extension.setupDependencyInjection
import extension.testCommonDependencies

/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-library")
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "io.element.android.libraries.wellknown.impl"
}

setupDependencyInjection()

dependencies {
    api(projects.libraries.wellknown.api)
    implementation(libs.androidx.annotationjvm)
    implementation(libs.coroutines.core)
    implementation(platform(libs.network.retrofit.bom))
    implementation(libs.network.retrofit)
    implementation(libs.serialization.json)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.core)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.network)

    testCommonDependencies(libs)
    testImplementation(libs.coroutines.core)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.services.toolbox.test)
}
