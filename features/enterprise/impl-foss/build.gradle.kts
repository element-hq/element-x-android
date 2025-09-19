import extension.setupDependencyInjection
import extension.testCommonDependencies

/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */
plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.features.enterprise.impl"
}

setupDependencyInjection()

dependencies {
    implementation(libs.compound)
    api(projects.features.enterprise.api)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.matrix.api)

    testCommonDependencies(libs)
    testImplementation(projects.libraries.matrix.test)
}
