/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

plugins {
    id("io.element.android-library")
    alias(libs.plugins.anvil)
}

android {
    namespace = "io.element.android.libraries.cryptography.impl"
}

anvil {
    generateDaggerFactories.set(true)
}

dependencies {
    anvil(projects.anvilcodegen)
    implementation(libs.dagger)
    implementation(projects.anvilannotations)
    implementation(projects.libraries.di)
    api(projects.libraries.cryptography.api)

    testImplementation(libs.test.junit)
    testImplementation(libs.test.truth)
}
