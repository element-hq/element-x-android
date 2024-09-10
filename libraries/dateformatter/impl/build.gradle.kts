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
    namespace = "io.element.android.libraries.dateformatter.impl"

    dependencies {
        anvil(projects.anvilcodegen)
        implementation(libs.dagger)
        implementation(projects.libraries.di)
        implementation(projects.anvilannotations)

        api(projects.libraries.dateformatter.api)
        api(libs.datetime)

        testImplementation(libs.test.junit)
        testImplementation(libs.test.truth)
        testImplementation(projects.libraries.dateformatter.test)
    }
}
