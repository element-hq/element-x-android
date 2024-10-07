/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

import java.net.URI

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
            url = URI("https://jitpack.io")
            content {
                includeModule("com.github.sergio-sastre.ComposablePreviewScanner", "android")
                includeModule("com.github.sergio-sastre.ComposablePreviewScanner", "core")
            }
        }
        // Snapshot versions
        maven {
            url = URI("https://s01.oss.sonatype.org/content/repositories/snapshots")
            content {
                includeModule("org.matrix.rustcomponents", "sdk-android")
                includeModule("io.element.android", "wysiwyg")
                includeModule("io.element.android", "wysiwyg-compose")
            }
        }
        // To have immediate access to Rust SDK versions without a sync with Maven Central
        maven {
            url = URI("https://s01.oss.sonatype.org/content/repositories/releases")
            content {
                includeModule("org.matrix.rustcomponents", "sdk-android")
            }
        }
        google()
        mavenCentral()
        maven {
            url = URI("https://www.jitpack.io")
            content {
                includeModule("com.github.UnifiedPush", "android-connector")
                includeModule("com.github.matrix-org", "matrix-analytics-events")
            }
        }
        flatDir {
            dirs("libraries/matrix/libs")
        }
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "TchapX"
include(":app")
include(":appnav")
include(":appconfig")
include(":appicon:element")
include(":appicon:enterprise")
include(":tests:konsist")
include(":tests:uitests")
include(":tests:testutils")
include(":anvilannotations")
include(":anvilcodegen")

include(":samples:minimal")

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
