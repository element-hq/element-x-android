import org.gradle.accessors.dm.LibrariesForLibs

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
    alias(libs.plugins.sonarqube)
}

tasks.register<Delete>("clean").configure {
    delete(rootProject.layout.buildDirectory)
}

private val ktLintVersion = the<LibrariesForLibs>().versions.ktlint.get()

allprojects {
    // Detekt
    apply {
        plugin("io.gitlab.arturbosch.detekt")
    }
    detekt {
        // preconfigure defaults
        buildUponDefaultConfig = true
        // activate all available (even unstable) rules.
        allRules = true
        // point to your custom config defining rules to run, overwriting default behavior
        config.from(files("$rootDir/tools/detekt/detekt.yml"))
    }
    dependencies {
        detektPlugins("io.nlopez.compose.rules:detekt:0.4.28")
        detektPlugins(project(":tests:detekt-rules"))
    }

    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        exclude("io/element/android/tests/konsist/failures/**")
    }

    // KtLint
    apply {
        plugin("org.jlleitschuh.gradle.ktlint")
    }

    // See https://github.com/JLLeitschuh/ktlint-gradle#configuration
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version = ktLintVersion
        android = true
        ignoreFailures = false
        enableExperimentalRules = true
        // display the corresponding rule
        verbose = true
        reporters {
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
            // To have XML report for Danger
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
        }
        val generatedPath = "${layout.buildDirectory.asFile.get()}/generated/"
        filter {
            exclude { element -> element.file.path.contains(generatedPath) }
            exclude("io/element/android/tests/konsist/failures/**")
        }
    }
    // Dependency check
    apply {
        plugin("org.owasp.dependencycheck")
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            // Warnings are potential errors, so stop ignoring them
            // This is disabled by default, but the CI will enforce this.
            // You can override by passing `-PallWarningsAsErrors=true` in the command line
            // Or add a line with "allWarningsAsErrors=true" in your ~/.gradle/gradle.properties file
            allWarningsAsErrors = project.properties["allWarningsAsErrors"] == "true"

            // Uncomment to suppress Compose Kotlin compiler compatibility warning
//            freeCompilerArgs.addAll(listOf("-P", "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true"))

            // Fix compilation warning for annotations
            // See https://youtrack.jetbrains.com/issue/KT-73255/Change-defaulting-rule-for-annotations for more details
            freeCompilerArgs.add("-Xannotation-default-target=first-only")
            // Opt-in to context receivers
            freeCompilerArgs.add("-Xcontext-parameters")
        }
    }
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

// To run a sonar analysis:
// Run './gradlew sonar -Dsonar.login=<SONAR_LOGIN>'
// The SONAR_LOGIN is stored in passbolt as Token Sonar Cloud Bma
// Sonar result can be found here: https://sonarcloud.io/project/overview?id=element-x-android
sonar {
    properties {
        property("sonar.projectName", "element-x-android")
        property("sonar.projectKey", "element-x-android")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.projectVersion", "1.0") // TODO project(":app").android.defaultConfig.versionName)
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.links.homepage", "https://github.com/element-hq/element-x-android/")
        property("sonar.links.ci", "https://github.com/element-hq/element-x-android/actions")
        property("sonar.links.scm", "https://github.com/element-hq/element-x-android/")
        property("sonar.links.issue", "https://github.com/element-hq/element-x-android/issues")
        property("sonar.organization", "element-hq")
        property("sonar.login", if (project.hasProperty("SONAR_LOGIN")) project.property("SONAR_LOGIN")!! else "invalid")

        // exclude source code from analyses separated by a colon (:)
        // Exclude Java source
        property("sonar.exclusions", "**/BugReporterMultipartBody.java")
    }
}

allprojects {
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
}

// Register quality check tasks.
tasks.register("runQualityChecks") {
    dependsOn(":tests:konsist:testDebugUnitTest")
    dependsOn(":app:lintGplayDebug")
    project.subprojects {
        tasks.findByPath("$path:lintDebug")?.let { dependsOn(it) }
        tasks.findByName("detekt")?.let { dependsOn(it) }
        tasks.findByName("ktlintCheck")?.let { dependsOn(it) }
        // tasks.findByName("buildHealth")?.let { dependsOn(it) }
    }
    dependsOn(":app:knitCheck")

    // Make sure all checks run even if some fail
    gradle.startParameter.isContinueOnFailure = true
}

// Make sure to delete old screenshots before recording new ones
subprojects {
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

// Make sure to delete old snapshot before recording new ones
subprojects {
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

subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            if (project.findProperty("composeCompilerReports") == "true") {
                freeCompilerArgs.addAll(
                    listOf(
                        "-P",
                        "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
                            "${project.layout.buildDirectory.asFile.get().absolutePath}/compose_compiler"
                    )
                )
            }
            if (project.findProperty("composeCompilerMetrics") == "true") {
                freeCompilerArgs.addAll(
                    listOf(
                        "-P",
                        "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
                            "${project.layout.buildDirectory.asFile.get().absolutePath}/compose_compiler"
                    )
                )
            }
        }
    }
}
