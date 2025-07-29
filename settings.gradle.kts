/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
        google()
        mavenCentral()
        maven {
            url = URI("https://www.jitpack.io")
            content {
                includeModule("com.github.matrix-org", "matrix-analytics-events")
            }
        }
        flatDir {
            dirs("libraries/matrix/libs")
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
include(":anvilannotations")
include(":anvilcodegen")

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
