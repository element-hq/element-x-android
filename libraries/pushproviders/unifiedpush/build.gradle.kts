import extension.setupAnvil

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */
plugins {
    id("io.element.android-library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "io.element.android.libraries.pushproviders.unifiedpush"
}

setupAnvil()

dependencies {
    implementation(libs.dagger)
    implementation(projects.features.enterprise.api)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.core)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.uiStrings)
    api(projects.libraries.troubleshoot.api)

    implementation(projects.libraries.pushstore.api)
    implementation(projects.libraries.pushproviders.api)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.core)
    implementation(projects.services.appnavstate.api)
    implementation(projects.services.toolbox.api)

    implementation(projects.libraries.network)
    implementation(platform(libs.network.okhttp.bom))
    implementation(libs.network.okhttp.okhttp)
    implementation(platform(libs.network.retrofit.bom))
    implementation(libs.network.retrofit)

    implementation(libs.serialization.json)

    // UnifiedPush library
    implementation(libs.unifiedpush)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.test.junit)
    testImplementation(libs.test.robolectric)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(libs.kotlinx.collections.immutable)
    testImplementation(projects.features.enterprise.test)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.push.test)
    testImplementation(projects.libraries.pushproviders.test)
    testImplementation(projects.libraries.pushstore.test)
    testImplementation(projects.tests.testutils)
    testImplementation(projects.services.toolbox.test)
    testImplementation(projects.services.appnavstate.test)
}
