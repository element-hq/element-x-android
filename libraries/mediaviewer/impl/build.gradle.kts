import extension.setupDependencyInjection
import extension.testCommonDependencies

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */
plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.libraries.mediaviewer.impl"
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

setupDependencyInjection()

dependencies {
    implementation(libs.coroutines.core)
    implementation(libs.coil.compose)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.telephoto.zoomableimage)
    implementation(libs.vanniktech.blurhash)
    implementation(libs.telephoto.flick)

    implementation(projects.features.enterprise.api)
    implementation(projects.features.viewfolder.api)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.audio.api)
    implementation(projects.libraries.core)
    implementation(projects.libraries.dateformatter.api)
    implementation(projects.libraries.di)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.featureflag.api)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.matrixmedia.api)
    implementation(projects.libraries.uiStrings)
    implementation(projects.libraries.voiceplayer.api)
    implementation(projects.services.toolbox.api)

    api(projects.libraries.mediaviewer.api)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.core)
    implementation(projects.libraries.di)
    implementation(projects.libraries.matrix.api)

    testCommonDependencies(libs, true)
    testImplementation(projects.features.enterprise.test)
    testImplementation(projects.libraries.audio.test)
    testImplementation(projects.libraries.dateformatter.test)
    testImplementation(projects.libraries.featureflag.test)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.matrixui)
    testImplementation(projects.libraries.mediaviewer.test)
    testImplementation(projects.services.toolbox.test)
    testImplementation(libs.coroutines.core)
}
