/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
}

android {
    namespace = "io.element.android.tests.konsist"
}

dependencies {
    val composeBom = platform(libs.androidx.compose.bom)
    testImplementation(composeBom)
    testImplementation(libs.androidx.compose.ui.tooling.preview)
    testImplementation(libs.test.junit)
    testImplementation(libs.test.konsist)
    testImplementation(libs.test.truth)
    testImplementation(projects.libraries.architecture)
    testImplementation(projects.libraries.designsystem)
}

// Make sure Konsist tests run for 'check' tasks. This is needed because otherwise we'd have to either:
// - Add every single module as a dependency of this one.
// - Move the Konsist tests to the `app` module, but the `app` module does not need to know about Konsist.
tasks.withType<Test>().configureEach {
    outputs.upToDateWhen {
        gradle.startParameter.taskNames.any { it.contains("check", ignoreCase = true).not() }
    }
}
