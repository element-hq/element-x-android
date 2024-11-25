import extension.setupAnvil

/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */
plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.features.enterprise.impl"
}

setupAnvil()

dependencies {
    implementation(projects.anvilannotations)
    api(projects.features.enterprise.api)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.matrix.api)

    testImplementation(libs.coroutines.test)
    testImplementation(libs.test.junit)
    testImplementation(libs.test.truth)
    testImplementation(projects.libraries.matrix.test)
}
