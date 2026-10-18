import extension.testCommonDependencies

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
    alias(libs.plugins.paparazzi)
}

android {
    namespace = "io.element.android.compound"

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

// Paparazzi's layoutlib runtime conflicts with Robolectric, so during Paparazzi record/verify runs
// only execute the screenshot tests (which use Paparazzi) and leave the Robolectric-based unit tests
// out. On plain unit test runs the screenshot package is excluded in the root build.gradle.kts.
tasks.withType(Test::class).configureEach {
    val isScreenshotTest = gradle.startParameter.taskNames.any { it.contains("paparazzi", ignoreCase = true) }
    if (isScreenshotTest) {
        include("**/screenshot/**")
    }
}

dependencies {
    testCommonDependencies(libs)

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
}
