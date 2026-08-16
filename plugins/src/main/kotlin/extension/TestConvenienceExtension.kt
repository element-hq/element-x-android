/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package extension

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType
import java.io.File

fun Project.setupTests() {
    tasks.withType<Test> {
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)

        val isScreenshotTest = project.gradle.startParameter.taskNames.any { it.contains("paparazzi", ignoreCase = true) }
        if (isScreenshotTest) {
            // Increase heap size for screenshot tests
            maxHeapSize = "2g"
            // Record all the languages?
            if (project.hasProperty("allLanguagesNoEnglish")) {
                // Do not record English language
                exclude("ui/*.class")
            } else if (project.hasProperty("allLanguages").not()) {
                // Do not record other languages
                exclude("translations/*.class")
            }
        } else {
            // Disable screenshot tests by default
            exclude("ui/*.class")
            exclude("translations/*.class")
        }
    }

    if (plugins.hasPlugin("com.android.library")) {
        removeOldPaparazziScreenshots()
        removeOldRoborazziScreenshots()
    }
}

private fun Project.removeOldPaparazziScreenshots() {
    // Make sure to delete old screenshots before recording new ones
    val snapshotsDir = File("${project.projectDir}/src/test/snapshots")
    val removeOldScreenshotsTask = tasks.register("removeOldSnapshots") {
        onlyIf { snapshotsDir.exists() }
        doFirst {
            println("Delete previous screenshots located at $snapshotsDir\n")
            snapshotsDir.deleteRecursively()
        }
    }
    tasks.findByName("recordPaparazzi")?.dependsOn(removeOldScreenshotsTask)
    tasks.findByName("recordPaparazziDebug")?.dependsOn(removeOldScreenshotsTask)
    tasks.findByName("recordPaparazziRelease")?.dependsOn(removeOldScreenshotsTask)
}

private fun Project.removeOldRoborazziScreenshots() {
    // Make sure to delete old snapshot before recording new ones
    val screenshotsDir = File("${project.projectDir}/screenshots")
    val removeOldScreenshotsTask = tasks.register("removeOldScreenshots") {
        onlyIf { screenshotsDir.exists() }
        doFirst {
            println("Delete previous screenshots located at $screenshotsDir\n")
            screenshotsDir.deleteRecursively()
        }
    }
    tasks.findByName("recordRoborazzi")?.dependsOn(removeOldScreenshotsTask)
    tasks.findByName("recordRoborazziDebug")?.dependsOn(removeOldScreenshotsTask)
    tasks.findByName("recordRoborazziRelease")?.dependsOn(removeOldScreenshotsTask)
}
