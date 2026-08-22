import extension.setupDependencyInjection
import extension.testCommonDependencies

/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "io.element.android.libraries.pushproviders.feral"
}

setupDependencyInjection()

dependencies {
    implementation(projects.appconfig)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.core)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.di)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.preferences.api)
    implementation(projects.libraries.push.api)
    implementation(projects.libraries.pushproviders.api)
    // UnifiedPushParser: the ntfy message body is the raw Matrix push-gateway notification JSON.
    implementation(projects.libraries.pushproviders.unifiedpush)
    implementation(projects.libraries.pushstore.api)
    implementation(projects.libraries.sessionStorage.api)
    implementation(projects.libraries.troubleshoot.api)
    implementation(projects.services.toolbox.api)

    implementation(libs.androidx.corektx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.startup)
    implementation(libs.coroutines.core)
    implementation(platform(libs.network.okhttp.bom))
    implementation(libs.network.okhttp)
    implementation(libs.serialization.json)

    testCommonDependencies(libs)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.preferences.test)
    testImplementation(projects.libraries.push.test)
    testImplementation(projects.libraries.pushproviders.test)
    testImplementation(projects.libraries.pushstore.test)
    testImplementation(projects.libraries.sessionStorage.test)
    testImplementation(projects.libraries.troubleshoot.test)
    testImplementation(projects.services.toolbox.test)
}
