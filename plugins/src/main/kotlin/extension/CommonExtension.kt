/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package extension

import Versions
import com.android.build.api.dsl.ApplicationDefaultConfig
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CompileOptions
import com.android.build.api.dsl.LibraryDefaultConfig
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.Lint
import isEnterpriseBuild
import org.gradle.api.Project
import java.io.File

fun ApplicationExtension.androidAppConfig(project: Project) {
    compileSdk = Versions.COMPILE_SDK

    defaultConfig(::defaultApplicationConfig)
    compileOptions(::defaultCompileOptions)
    testOptions(::defaultTestOptions)

    lint {
        project.defaultLintOptions(this)
    }
}

fun LibraryExtension.androidLibraryConfig(project: Project) {
    compileSdk = Versions.COMPILE_SDK

    defaultConfig(::defaultLibraryConfig)
    compileOptions(::defaultCompileOptions)
    testOptions(::defaultTestOptions)

    lint {
        project.defaultLintOptions(this)
    }
}

fun ApplicationExtension.composeAppConfig() {
    buildFeatures(::defaultComposeBuildFeatures)
    packaging(::defaultPackagingOptions)
    lint(::defaultComposeLintOptions)
}

fun LibraryExtension.composeLibraryConfig() {
    buildFeatures(::defaultComposeBuildFeatures)
    packaging(::defaultPackagingOptions)
    lint(::defaultComposeLintOptions)
}

fun defaultApplicationConfig(applicationDefaultConfig: ApplicationDefaultConfig) = applicationDefaultConfig.apply {
    minSdk = Versions.minSdk
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    vectorDrawables {
        useSupportLibrary = true
        generatedDensities()
    }
}

fun defaultLibraryConfig(libraryDefaultConfig: LibraryDefaultConfig) = libraryDefaultConfig.apply {
    minSdk = Versions.minSdk
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    vectorDrawables {
        useSupportLibrary = true
        generatedDensities()
    }
}

fun defaultCompileOptions(compileOptions: CompileOptions) = compileOptions.apply {
    sourceCompatibility = Versions.javaVersion
    targetCompatibility = Versions.javaVersion
}

fun defaultTestOptions(testOptions: com.android.build.api.dsl.TestOptions) = testOptions.apply {
    unitTests.isReturnDefaultValues = true
}

fun defaultComposeBuildFeatures(buildFeatures: com.android.build.api.dsl.BuildFeatures) = buildFeatures.apply {
    compose = true
}

fun defaultPackagingOptions(packagingOptions: com.android.build.api.dsl.Packaging) = packagingOptions.apply {
    resources.excludes.apply {
        add("META-INF/AL2.0")
        add("META-INF/LGPL2.1")
    }
}

fun defaultComposeLintOptions(lint: Lint) = lint.apply {
    // Extra rules for compose
    // Disabled until lint stops inspecting generated ksp files...
    // error.add("ComposableLambdaParameterNaming")
    error.add("ComposableLambdaParameterPosition")
    ignoreTestFixturesSources = true
    checkGeneratedSources = false
}

fun Project.defaultLintOptions(lint: Lint) = lint.apply {
    lintConfig = File("${project.rootDir}/tools/lint/lint.xml")
    if (isEnterpriseBuild) {
        // Disable check on ObsoleteSdkInt for Enterprise builds
        // since the min sdk is higher for Enterprise builds
        disable.add("ObsoleteSdkInt")
    }
    checkDependencies = false
    abortOnError = true
    ignoreTestSources = true
    ignoreTestFixturesSources = true
    checkGeneratedSources = false
}
