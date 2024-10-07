import extension.setupAnvil

/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

plugins {
    id("io.element.android-library")
}

setupAnvil()

android {
    namespace = "io.element.android.libraries.dateformatter.impl"

    dependencies {
        implementation(libs.dagger)
        implementation(projects.libraries.di)

        api(projects.libraries.dateformatter.api)
        api(libs.datetime)

        testImplementation(libs.test.junit)
        testImplementation(libs.test.truth)
        testImplementation(projects.libraries.dateformatter.test)
    }
}
