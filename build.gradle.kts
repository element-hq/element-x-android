/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("io.element.android-root")
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.dependencycheck) apply false
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.dependencyanalysis)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.dependencygraph)
    alias(libs.plugins.sonarqube) apply false
}

tasks.register<Delete>("clean").configure {
    delete(rootProject.layout.buildDirectory)
}

// See https://github.com/autonomousapps/dependency-analysis-android-gradle-plugin/wiki/Customizing-plugin-behavior
dependencyAnalysis {
    issues {
        all {
            onUnusedDependencies {
                exclude("com.jakewharton.timber:timber")
            }
            onUnusedAnnotationProcessors {}
            onRedundantPlugins {}
            onIncorrectConfiguration {}
        }
    }
}

// Register quality check tasks.
tasks.register("runQualityChecks") {
    dependsOn(":tests:konsist:testDebugUnitTest")
    dependsOn(":app:lintGplayDebug")
    dependsOn("checkDocs")
}

// Register Markdown documentation check task.
tasks.register("checkDocs", Exec::class.java) {
    inputs.files("./*.md", "docs/**/*.md")
    commandLine("python3", "tools/docs/generate_toc.py", "--verify", *inputs.files.map { it.path }.toTypedArray())
}

// Register Markdown documentation TOC generation task.
tasks.register("generateDocsToc", Exec::class.java) {
    inputs.files("./*.md", "docs/**/*.md")
    commandLine("python3", "tools/docs/generate_toc.py", *inputs.files.map { it.path }.toTypedArray())
}
