/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

/**
 * This will generate the plugin "io.element.android-compose-application" to use by app and samples modules
 */
import extension.androidConfig
import extension.commonDependencies
import extension.composeConfig
import extension.composeDependencies
import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()
plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.autonomousapps.dependency-analysis")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    androidConfig(project)
    composeConfig()
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
    composeDependencies(libs)
    coreLibraryDesugaring(libs.android.desugar)
}
