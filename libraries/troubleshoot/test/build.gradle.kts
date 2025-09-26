/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */
plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.libraries.troubleshoot.test"
}

dependencies {
    implementation(projects.libraries.troubleshoot.api)
    implementation(projects.tests.testutils)
    implementation(libs.coroutines.test)
    implementation(libs.test.core)
    implementation(libs.test.turbine)
}

ktlint {
    filter {
        exclude { element ->
            val path = element.file.path
            // Exclude this file, that ktlint cannot parse.
            path.contains("libraries/troubleshoot/test/src/main/kotlin/io/element/android/libraries/troubleshoot/test/Utils.kt")
        }
    }
}
