/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

import extension.allFeaturesImpl
import extension.allLibrariesImpl
import extension.allServicesImpl

plugins {
    id("io.element.android-compose-library")
    alias(libs.plugins.paparazzi)
}

android {
    // Keep it as short as possible
    namespace = "ui"
}

dependencies {
    // Paparazzi 1.3.2 workaround (see https://github.com/cashapp/paparazzi/blob/master/CHANGELOG.md#132---2024-01-13)
    constraints.add("testImplementation", "com.google.guava:guava") {
        attributes {
            attribute(
                TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE,
                objects.named(TargetJvmEnvironment::class.java, TargetJvmEnvironment.STANDARD_JVM)
            )
        }
        because(
            "LayoutLib and sdk-common depend on Guava's -jre published variant." +
                "See https://github.com/cashapp/paparazzi/issues/906."
        )
    }

    implementation(libs.showkase)

    // TODO There is a Resources.NotFoundException maybe due to the mipmap, even if we have
    // `testOptions { unitTests.isIncludeAndroidResources = true }` in the app build.gradle.kts file
    // implementation(projects.app)
    implementation(projects.appnav)
    allLibrariesImpl()
    allServicesImpl()
    allFeaturesImpl(project)
    implementation(projects.appicon.element)
    implementation(projects.appicon.enterprise)

    testImplementation(libs.test.junit)
    testImplementation(libs.test.parameter.injector)
    testImplementation(projects.libraries.designsystem)
    testImplementation(libs.test.composable.preview.scanner)
}
