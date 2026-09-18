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

private val catalog = the<LibrariesForLibs>()

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
        detektPlugins(catalog.detekt.compose.rules)
        detektPlugins(project(":tests:detekt-rules"))
    }

    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        exclude("io/element/android/tests/konsist/failures/**")

        // This file comes from another project and we want to keep it as close to the original as possible
        exclude("org/rustls/platformverifier/**")
    }

    // KtLint
    apply {
        plugin("org.jlleitschuh.gradle.ktlint")
    }

    // See https://github.com/JLLeitschuh/ktlint-gradle#configuration
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version = catalog.versions.ktlint.get()
        android = true
        ignoreFailures = false
        enableExperimentalRules = true
        // display the corresponding rule
        verbose = true
        reporters {
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
            // To have XML report for the CI to annotate the PR, see .github/workflows/quality.yml
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
        }
        val generatedPath = "${layout.buildDirectory.asFile.get()}/generated/"
        filter {
            exclude { element -> element.file.path.contains(generatedPath) }
            exclude("io/element/android/tests/konsist/failures/**")

            // This file comes from another project and we want to keep it as close to the original as possible
            exclude("**/SafeChildrenTransitionScope.kt")

            // This file comes from another project and we want to keep it as close to the original as possible
            exclude("org/rustls/platformverifier/**")
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
            allWarningsAsErrors = findProperty("allWarningsAsErrors") == "true"

            // Uncomment to suppress Compose Kotlin compiler compatibility warning
//            freeCompilerArgs.addAll(listOf("-P", "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true"))

            // Fix compilation warning for annotations
            // See https://youtrack.jetbrains.com/issue/KT-73255/Change-defaulting-rule-for-annotations for more details
            freeCompilerArgs.add("-Xannotation-default-target=first-only")
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
        val isRoborazziTest = project.gradle.startParameter.taskNames.any { it.contains("roborazzi", ignoreCase = true) }
        if (isScreenshotTest) {
            // Paparazzi tests benefit from parallelisation, so we can use half the available cores to run them in parallel.
            maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
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
            // Robolectric and Compose pay a 5 to 30 seconds bootstrap cost per test JVM (instrumenting
            // android-all, then warming up the Compose runtime). That cost is paid once per JVM and then
            // amortised over every test class the JVM runs, so splitting a module across several forks
            // re-pays it for each fork instead of saving time. Keep a single fork per module and let
            // Gradle parallelise by running many modules' test tasks concurrently instead.
            maxParallelForks = 1

            // Disable screenshot tests by default
            exclude("ui/*.class")
            exclude("translations/*.class")
            if (isRoborazziTest.not()) {
                // Roborazzi screenshot tests (:libraries:compound) live in a `screenshot` package, which
                // the two patterns above do not match, so they used to run on every plain unit test run.
                // They are verified by the dedicated `verifyRoborazziDebug` task instead.
                exclude("**/screenshot/**")
            }
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
    dependsOn("checkDocs")
    // Make sure all checks run even if some fail
    gradle.startParameter.isContinueOnFailure = true
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

// Delete all the screenshots. Not hooked to the record tasks on purpose, see `pruneObsoleteSnapshots`
// below. Run it by hand (or use Paparazzi's own `cleanRecordPaparazziDebug`) to record from scratch.
subprojects {
    val snapshotsDir = File("${project.projectDir}/src/test/snapshots")
    tasks.register("removeOldSnapshots") {
        onlyIf { snapshotsDir.exists() }
        doFirst {
            println("Delete previous screenshots located at $snapshotsDir\n")
            snapshotsDir.deleteRecursively()
        }
    }
}

// Delete the screenshots of previews that no longer exist, *after* recording.
//
// Wiping the whole snapshots directory before recording used to be how stale screenshots were removed,
// but it also removes the reference image Paparazzi needs to honour
// `app.cash.paparazzi.overwriteOnMaxPercentDifference` (see gradle.properties): with no existing file to
// compare against, every screenshot is rewritten unconditionally, so renderer noise far below the
// threshold `verifyPaparazziDebug` accepts still rewrites the Git LFS blob of every affected screenshot.
//
// Instead, let the record task keep the existing screenshots, then prune the ones belonging to a
// preview that Paparazzi did not render at all.
//
// Pruning per preview, and not per screenshot, is deliberate: a preview that fails to render is absent
// from the run report in exactly the same way as a preview that was deleted, and LayoutLib does fail to
// render a preview now and then (see LayoutLibErrorFilterStatement). Matching whole previews means such
// a failure leaves the other screenshots of that preview in place, so it cannot silently delete a
// screenshot that is still in use. The cost is that dropping one value from a PreviewParameterProvider
// leaves its last screenshot behind, since the preview itself still renders. Run `removeOldSnapshots`
// and record again to clean those up.
subprojects {
    val projectDir = project.projectDir
    val pruneObsoleteSnapshots = tasks.register("pruneObsoleteSnapshots") {
        description = "Delete the recorded screenshots of previews that no longer exist"
        val imagesDir = File(projectDir, "src/test/snapshots/images")
        val reportsDir = File(projectDir, "build/reports/paparazzi")
        onlyIf { imagesDir.exists() }
        doLast {
            // One `runs/<runName>.js` per forked test JVM, each holding `window.runs[...] = [ ...snapshots... ];`.
            val runFiles = reportsDir.walkTopDown()
                .filter { it.isFile && it.extension == "js" && it.parentFile.name == "runs" }
                .toList()
            check(runFiles.isNotEmpty()) {
                "No Paparazzi run report found under $reportsDir, so there is no way to tell which " +
                    "screenshots are still in use. Refusing to delete anything."
            }
            // `testName` is serialised as "<package>.<class>#<method>" and the screenshot file is named
            // "<package>_<class>_<method>.png". Only the package and the class are used here, they identify
            // the preview. Do not be tempted to rebuild the whole file name from the method: it has to be
            // normalised exactly as `Snapshot.toFileName` does, which replaces whitespace with the
            // delimiter, so a preview named "List item - Simple" does not read as its own file name.
            val testNameRegex = """"testName"\s*:\s*"(.*)\.([^.]*)#""".toRegex()
            val renderedPreviews = runFiles.flatMap { runFile ->
                testNameRegex.findAll(runFile.readText()).map { match ->
                    val (packageName, className) = match.destructured
                    // Keep the trailing delimiter, so that `Foo_` does not also match `FooBar_…`.
                    "${packageName}_${className}_"
                }
            }.toSet()
            check(renderedPreviews.isNotEmpty()) {
                "Could not read any snapshot name out of the Paparazzi run reports in $reportsDir. " +
                    "The report format has probably changed. Refusing to delete anything."
            }

            val existing = imagesDir.listFiles { file -> file.extension == "png" }.orEmpty()
            val obsolete = existing.filter { file -> renderedPreviews.none { file.name.startsWith(it) } }
            // Deleting a screenshot is a tracked file deletion, and a bad `renderedPreviews` set would wipe
            // thousands of them. Anything beyond a trickle means something is wrong rather than a few
            // deleted previews.
            check(obsolete.size <= existing.size / 10) {
                "pruneObsoleteSnapshots would delete ${obsolete.size} of ${existing.size} screenshots in " +
                    "$imagesDir, which looks like a bug rather than deleted previews. Refusing to delete " +
                    "anything. If the deletions are expected, run `removeOldSnapshots` and record again."
            }
            obsolete.forEach {
                println("Delete obsolete screenshot ${it.name}")
                it.delete()
            }
            println("Pruned ${obsolete.size} obsolete screenshot(s), kept ${existing.size - obsolete.size}.\n")
        }
    }
    // `configureEach` on a live filtered collection, and not `findByName`, because the Paparazzi plugin
    // registers its record tasks per variant, long after this block runs.
    tasks.matching { it.name.startsWith("recordPaparazzi") }.configureEach {
        finalizedBy(pruneObsoleteSnapshots)
    }
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
