/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

pluginManagement {
    repositories {
        includeBuild("plugins")
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url = uri("https://www.jitpack.io")
            content {
                includeModule("com.github.matrix-org", "matrix-analytics-events")
                // Required transitively by androidx.media3:media3-exoplayer-midi for MIDI playback.
                includeModule("com.github.philburk", "jsyn")
                // This is a fork of Konsist that skips hidden folders and files, which otherwise can cause performance issues.
                includeModule("com.github.jmartinesp", "konsist")
            }
        }
        // Check for official Android-related packages only in Google's maven repo
        exclusiveContent {
            forRepository {
                google()
            }
            filter {
                includeGroupByRegex("android\\..*")
                includeGroupByRegex("androidx\\..*")
                includeGroupByRegex("com\\.android\\.tools.*")
                includeGroupByRegex("com\\.google\\.firebase.*")
                includeGroupByRegex("com\\.google\\.android.*")
            }
        }
        mavenCentral()
        maven {
            url = uri("https://repo1.maven.org/maven2/")
        }
        flatDir {
            dirs("libraries/matrix/libs")
        }
        // Temporary: the native call component and the matrix-rust-rtc core publish as GitHub release
        // assets, which carry full Maven metadata. Both blocks go once they reach Maven Central.
        ivy {
            url = uri("https://github.com/element-hq/element-call-android/releases/download")
            patternLayout { artifact("v[revision]/[artifact]-[revision](-[classifier])(.[ext])") }
            metadataSources { gradleMetadata() }
            content {
                // Per module: `element-call.*` would also catch element-call-embedded, the WebView call.
                includeModule("io.element.android", "element-call-bom")
                includeModule("io.element.android", "element-call-api")
                includeModule("io.element.android", "element-call")
                includeModule("io.element.android", "element-call-ui")
                includeModule("io.element.android", "element-call-matrix")
                includeModule("io.element.android", "element-call-test")
            }
        }
        ivy {
            url = uri("https://github.com/element-hq/matrix-rust-rtc/releases/download")
            patternLayout { artifact("v[revision]/[artifact]-[revision](-[classifier])(.[ext])") }
            // The core ships an AAR and nothing else; element-call's module metadata names it with an
            // explicit aar artifact selector, so it needs no POM.
            metadataSources { artifact() }
            content { includeModule("io.element.android", "matrix-rtc-android") }
        }
        // Also temporary: testing an unreleased element-call-android build. Publish it there with
        // `publishToMavenLocal -PVERSION_NAME=0.1.0-local`, then build here with
        // `-PelementCallLocalVersion=0.1.0-local`. Absent without the property, so that everything
        // resolves for a contributor who has never built the library.
        providers.gradleProperty("elementCallLocalVersion").orNull?.let { elementCallVersion ->
            logger.lifecycle("Note: resolving io.element.android:element-call-* $elementCallVersion from mavenLocal")
            mavenLocal {
                content {
                    // Per module: compound-android and element-call-embedded stay on Maven Central.
                    includeModule("io.element.android", "element-call-bom")
                    includeModule("io.element.android", "element-call-api")
                    includeModule("io.element.android", "element-call")
                    includeModule("io.element.android", "element-call-ui")
                    includeModule("io.element.android", "element-call-matrix")
                    includeModule("io.element.android", "element-call-test")
                }
            }
        }
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "ElementX"
include(":app")
include(":appnav")
include(":appconfig")
include(":appicon:element")
include(":appicon:enterprise")
include(":tests:detekt-rules")
include(":tests:konsist")
include(":tests:uitests")
include(":tests:testutils")
include(":annotations")
include(":codegen")

fun includeProjects(directory: File, path: String, maxDepth: Int = 1) {
    directory.listFiles().orEmpty().also { it.sort() }.forEach { file ->
        if (file.isDirectory) {
            val newPath = "$path:${file.name}"
            val buildFile = File(file, "build.gradle.kts")
            if (buildFile.exists()) {
                include(newPath)
                logger.lifecycle("Included project: $newPath")
            } else if (maxDepth > 0) {
                includeProjects(file, newPath, maxDepth - 1)
            }
        }
    }
}

includeProjects(File(rootDir, "enterprise"), ":enterprise", maxDepth = 2)
includeProjects(File(rootDir, "features"), ":features")
includeProjects(File(rootDir, "libraries"), ":libraries")
includeProjects(File(rootDir, "services"), ":services")

// Uncomment to include the compound-android module as a local dependency so you can work on it locally.
// You will also need to clone it in the specified folder.
// includeBuild("checkouts/compound-android") {
//    dependencySubstitution {
//        // substitute remote dependency with local module
//        substitute(module("io.element.android:compound-android")).using(project(":compound"))
//    }
// }
