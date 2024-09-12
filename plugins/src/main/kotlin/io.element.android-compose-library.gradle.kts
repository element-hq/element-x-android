/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

/**
 * This will generate the plugin "io.element.android-compose-library", used in android library with compose modules.
 */
import extension.androidConfig
import extension.commonDependencies
import extension.composeConfig
import extension.composeDependencies
import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()
plugins {
    id("com.android.library")
    id("kotlin-android")
    id("com.autonomousapps.dependency-analysis")
}

android {
    androidConfig(project)
    composeConfig(libs)
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    commonDependencies(libs)
    composeDependencies(libs)
    coreLibraryDesugaring(libs.android.desugar)
}
