import extension.setupDependencyInjection
import extension.testCommonDependencies

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */
plugins {
    id("io.element.android-library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "io.element.android.libraries.pushproviders.unifiedpush"
}

setupDependencyInjection()

dependencies {
    implementation(projects.features.enterprise.api)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.core)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.push.api)
    implementation(projects.libraries.uiStrings)
    api(projects.libraries.troubleshoot.api)

    implementation(projects.libraries.pushstore.api)
    implementation(projects.libraries.pushproviders.api)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.core)
    implementation(projects.services.toolbox.api)

    implementation(projects.libraries.network)
    implementation(platform(libs.network.okhttp.bom))
    implementation(libs.network.okhttp.okhttp)
    implementation(platform(libs.network.retrofit.bom))
    implementation(libs.network.retrofit)

    implementation(libs.serialization.json)

    // UnifiedPush library
    api(libs.unifiedpush)

    testCommonDependencies(libs)
    testImplementation(libs.kotlinx.collections.immutable)
    testImplementation(projects.features.enterprise.test)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.push.test)
    testImplementation(projects.libraries.pushproviders.test)
    testImplementation(projects.libraries.pushstore.test)
    testImplementation(projects.libraries.troubleshoot.test)
    testImplementation(projects.services.toolbox.test)
}
