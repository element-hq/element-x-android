/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

/**
 * This will generate the plugin "io.element.android-library", used in android library without compose modules.
 */
import extension.androidConfig
import extension.commonDependencies
import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()
plugins {
    id("com.android.library")
    id("kotlin-android")
    id("com.autonomousapps.dependency-analysis")
}

android {
    androidConfig(project)
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

kotlin {
    jvmToolchain {
        languageVersion = Versions.javaLanguageVersion
    }
}

dependencies {
    commonDependencies(libs)
    coreLibraryDesugaring(libs.android.desugar)
}
