import extension.setupAnvil

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */
plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.services.analyticsproviders.posthog"
}

setupAnvil()

dependencies {
    implementation(libs.dagger)
    implementation(libs.posthog) {
        exclude("com.android.support", "support-annotations")
    }
    implementation(projects.libraries.core)
    implementation(projects.libraries.di)
    implementation(projects.services.analyticsproviders.api)

    testImplementation(libs.coroutines.test)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.junit)
    testImplementation(projects.tests.testutils)
    testImplementation(libs.test.mockk)
}
