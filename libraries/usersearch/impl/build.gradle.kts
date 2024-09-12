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
    namespace = "io.element.android.libraries.usersearch.impl"
}

anvil {
    generateDaggerFactories.set(true)
}

dependencies {
    implementation(projects.libraries.core)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.di)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.matrix.api)
    api(projects.libraries.usersearch.api)
    implementation(libs.kotlinx.collections.immutable)

    testImplementation(libs.test.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.molecule.runtime)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.usersearch.test)
}
