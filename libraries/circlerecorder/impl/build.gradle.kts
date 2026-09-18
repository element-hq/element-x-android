import extension.setupDependencyInjection
import extension.testCommonDependencies

/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */
plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.libraries.circlerecorder.impl"
}

setupDependencyInjection()

dependencies {
    api(projects.libraries.circlerecorder.api)

    implementation(projects.appconfig)
    implementation(projects.libraries.core)
    implementation(projects.libraries.di)
    implementation(projects.libraries.androidutils)

    implementation(libs.androidx.annotationjvm)
    implementation(libs.coroutines.core)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.video)
    implementation(libs.androidx.media3.transformer)
    implementation(libs.androidx.media3.common)

    testCommonDependencies(libs)
    testImplementation(libs.coroutines.core)
}
