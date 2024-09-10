/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

plugins {
    id("io.element.android-library")
    alias(libs.plugins.anvil)
}

anvil {
    generateDaggerFactories.set(true)
}

android {
    namespace = "io.element.android.services.appnavstate.impl"
}

dependencies {
    anvil(projects.anvilcodegen)
    implementation(libs.dagger)
    implementation(projects.libraries.core)
    implementation(projects.libraries.di)
    implementation(projects.libraries.matrix.api)
    implementation(projects.anvilannotations)

    implementation(libs.coroutines.core)
    implementation(libs.androidx.corektx)
    implementation(libs.androidx.lifecycle.process)

    api(projects.services.appnavstate.api)

    testImplementation(libs.test.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.test.truth)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.tests.testutils)
    testImplementation(projects.services.appnavstate.test)
}
