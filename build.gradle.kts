import com.google.devtools.ksp.gradle.KspTask
import kotlinx.kover.api.KoverTaskExtension
import org.apache.tools.ant.taskdefs.optional.ReplaceRegExp
import org.jetbrains.kotlin.cli.common.toBooleanLenient

buildscript {
    dependencies {
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.gms.google.services)
    }
}

/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.anvil) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kapt) apply false
    alias(libs.plugins.dependencycheck) apply false
    alias(libs.plugins.dependencyanalysis)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.dependencygraph)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.kover)
}

tasks.register<Delete>("clean").configure {
    delete(rootProject.layout.buildDirectory)
}

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
        detektPlugins("io.nlopez.compose.rules:detekt:0.3.8")
    }

    // KtLint
    apply {
        plugin("org.jlleitschuh.gradle.ktlint")
    }

    // See https://github.com/JLLeitschuh/ktlint-gradle#configuration
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        // See https://github.com/pinterest/ktlint/releases/
        // TODO Regularly check for new version here ^
        version.set("0.48.2")
        android.set(true)
        ignoreFailures.set(false)
        enableExperimentalRules.set(true)
        // display the corresponding rule
        verbose.set(true)
        reporters {
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
            // To have XML report for Danger
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
        }
        filter {
            exclude { element -> element.file.path.contains("${layout.buildDirectory.asFile.get()}/generated/") }
        }
    }
    // Dependency check
    apply {
        plugin("org.owasp.dependencycheck")
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        // Warnings are potential errors, so stop ignoring them
        // This is disabled by default, but the CI will enforce this.
        // You can override by passing `-PallWarningsAsErrors=true` in the command line
        // Or add a line with "allWarningsAsErrors=true" in your ~/.gradle/gradle.properties file
        kotlinOptions.allWarningsAsErrors = project.properties["allWarningsAsErrors"] == "true"

        kotlinOptions {
            // Uncomment to suppress Compose Kotlin compiler compatibility warning
            /*
            freeCompilerArgs += listOf(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true"
            )
             */
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
        }
    }
}

// To run a sonar analysis:
// Run './gradlew sonar -Dsonar.login=<SONAR_LOGIN>'
// The SONAR_LOGIN is stored in passbolt as Token Sonar Cloud Bma
// Sonar result can be found here: https://sonarcloud.io/project/overview?id=vector-im_element-x-android
sonar {
    properties {
        property("sonar.projectName", "element-x-android")
        property("sonar.projectKey", "vector-im_element-x-android")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.projectVersion", "1.0") // TODO project(":app").android.defaultConfig.versionName)
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.links.homepage", "https://github.com/element-hq/element-x-android/")
        property("sonar.links.ci", "https://github.com/element-hq/element-x-android/actions")
        property("sonar.links.scm", "https://github.com/element-hq/element-x-android/")
        property("sonar.links.issue", "https://github.com/element-hq/element-x-android/issues")
        property("sonar.organization", "new_vector_ltd_organization")
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
            maxHeapSize = "1g"
        } else {
            // Disable screenshot tests by default
            exclude("ui/S.class")
        }
    }
}

allprojects {
    apply(plugin = "kover")
}

// https://kotlin.github.io/kotlinx-kover/
// Run `./gradlew koverMergedHtmlReport` to get report at ./build/reports/kover
// Run `./gradlew koverMergedReport` to also get XML report
koverMerged {
    enable()

    filters {
        classes {
            excludes.addAll(
                listOf(
                    // Exclude generated classes.
                    "*_ModuleKt",
                    "anvil.hint.binding.io.element.*",
                    "anvil.hint.merge.*",
                    "anvil.hint.multibinding.io.element.*",
                    "anvil.module.*",
                    "com.airbnb.android.showkase*",
                    "io.element.android.libraries.designsystem.showkase.*",
                    "io.element.android.x.di.DaggerAppComponent*",
                    "*_Factory",
                    "*_Factory_Impl",
                    "*_Factory$*",
                    "*_Module",
                    "*_Module$*",
                    "*Module_Provides*",
                    "Dagger*Component*",
                    "*ComposableSingletons$*",
                    "*_AssistedFactory_Impl*",
                    "*BuildConfig",
                    // Generated by Showkase
                    "*Ioelementandroid*PreviewKt$*",
                    "*Ioelementandroid*PreviewKt",
                    // Other
                    // We do not cover Nodes (normally covered by maestro, but code coverage is not computed with maestro)
                    "*Node",
                    "*Node$*",
                    // Exclude `:libraries:matrix:impl` module, it contains only wrappers to access the Rust Matrix SDK api, so it is not really relevant to unit test it: there is no logic to test.
                    "io.element.android.libraries.matrix.impl.*",
                    "*Presenter\$present\$*",
                    // Forked from compose
                    "io.element.android.libraries.designsystem.theme.components.bottomsheet.*",
                )
            )
        }

        annotations {
            excludes.addAll(
                listOf(
                    "*Preview",
                )
            )
        }

        projects {
            excludes.addAll(
                listOf(
                    ":anvilannotations",
                    ":anvilcodegen",
                    ":samples:minimal",
                    ":tests:testutils",
                )
            )
        }
    }

    // Run ./gradlew koverMergedVerify to check the rules.
    verify {
        // Does not seems to work, so also run the task manually on the workflow.
        onCheck.set(true)
        // General rule: minimum code coverage.
        rule {
            name = "Global minimum code coverage."
            target = kotlinx.kover.api.VerificationTarget.ALL
            bound {
                minValue = 65
                // Setting a max value, so that if coverage is bigger, it means that we have to change minValue.
                // For instance if we have minValue = 20 and maxValue = 30, and current code coverage is now 31.32%, update
                // minValue to 25 and maxValue to 35.
                maxValue = 75
                counter = kotlinx.kover.api.CounterType.INSTRUCTION
                valueType = kotlinx.kover.api.VerificationValueType.COVERED_PERCENTAGE
            }
        }
        // Rule to ensure that coverage of Presenters is sufficient.
        rule {
            name = "Check code coverage of presenters"
            target = kotlinx.kover.api.VerificationTarget.CLASS
            overrideClassFilter {
                includes += "*Presenter"
                excludes += "*Fake*Presenter"
                excludes += "io.element.android.appnav.loggedin.LoggedInPresenter$*"
                // Some options can't be tested at the moment
                excludes += "io.element.android.features.preferences.impl.developer.DeveloperSettingsPresenter$*"
                excludes += "*Presenter\$present\$*"
            }
            bound {
                minValue = 85
                counter = kotlinx.kover.api.CounterType.INSTRUCTION
                valueType = kotlinx.kover.api.VerificationValueType.COVERED_PERCENTAGE
            }
        }
        // Rule to ensure that coverage of States is sufficient.
        rule {
            name = "Check code coverage of states"
            target = kotlinx.kover.api.VerificationTarget.CLASS
            overrideClassFilter {
                includes += "^*State$"
                excludes += "io.element.android.appnav.root.RootNavState*"
                excludes += "io.element.android.libraries.matrix.api.timeline.item.event.OtherState$*"
                excludes += "io.element.android.libraries.matrix.api.timeline.item.event.EventSendState$*"
                excludes += "io.element.android.libraries.matrix.api.room.RoomMembershipState*"
                excludes += "io.element.android.libraries.matrix.api.room.MatrixRoomMembersState*"
                excludes += "io.element.android.libraries.push.impl.notifications.NotificationState*"
                excludes += "io.element.android.features.messages.impl.media.local.pdf.PdfViewerState"
                excludes += "io.element.android.features.messages.impl.media.local.LocalMediaViewState"
                excludes += "io.element.android.features.location.impl.map.MapState*"
                excludes += "io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState*"
                excludes += "io.element.android.libraries.designsystem.swipe.SwipeableActionsState*"
                excludes += "io.element.android.features.messages.impl.timeline.components.ExpandableState*"
                excludes += "io.element.android.features.messages.impl.timeline.model.bubble.BubbleState*"
                excludes += "io.element.android.libraries.maplibre.compose.CameraPositionState*"
                excludes += "io.element.android.libraries.maplibre.compose.SaveableCameraPositionState"
                excludes += "io.element.android.libraries.maplibre.compose.SymbolState*"
                excludes += "io.element.android.features.ftue.api.state.*"
                excludes += "io.element.android.features.ftue.impl.welcome.state.*"
            }
            bound {
                minValue = 90
                counter = kotlinx.kover.api.CounterType.INSTRUCTION
                valueType = kotlinx.kover.api.VerificationValueType.COVERED_PERCENTAGE
            }
        }
        // Rule to ensure that coverage of Views is sufficient (deactivated for now).
        rule {
            name = "Check code coverage of views"
            target = kotlinx.kover.api.VerificationTarget.CLASS
            overrideClassFilter {
                includes += "*ViewKt"
            }
            bound {
                // TODO Update this value, for now there are too many missing tests.
                minValue = 0
                counter = kotlinx.kover.api.CounterType.INSTRUCTION
                valueType = kotlinx.kover.api.VerificationValueType.COVERED_PERCENTAGE
            }
        }
    }
}

// When running on the CI, run only debug test variants
val ciBuildProperty = "ci-build"
val isCiBuild = if (project.hasProperty(ciBuildProperty)) {
    val raw = project.property(ciBuildProperty) as? String
    raw?.toBooleanLenient() == true || raw?.toIntOrNull() == 1
} else {
    false
}
if (isCiBuild) {
    allprojects {
        afterEvaluate {
            tasks.withType<Test>().configureEach {
                extensions.configure<KoverTaskExtension> {
                    val enabled = name.contains("debug", ignoreCase = true)
                    isDisabled.set(!enabled)
                }
            }
        }
    }
}

// Register quality check tasks.
tasks.register("runQualityChecks") {
    project.subprojects {
        // For some reason `findByName("lint")` doesn't work
        tasks.findByPath("$path:lint")?.let { dependsOn(it) }
        tasks.findByName("detekt")?.let { dependsOn(it) }
        tasks.findByName("ktlintCheck")?.let { dependsOn(it) }
        // tasks.findByName("buildHealth")?.let { dependsOn(it) }
    }
    dependsOn(":app:knitCheck")
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

// Workaround for https://github.com/airbnb/Showkase/issues/335
subprojects {
    tasks.withType<KspTask>() {
        doLast {
            fileTree(layout.buildDirectory).apply { include("**/*ShowkaseExtension*.kt") }.files.forEach { file ->
                ReplaceRegExp().apply {
                    setMatch("^public fun Showkase.getMetadata")
                    setReplace("@Suppress(\"DEPRECATION\") public fun Showkase.getMetadata")
                    setFlags("g")
                    setByLine(true)
                    setFile(file)
                    execute()
                }
            }
        }
    }
}

subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            if (project.findProperty("composeCompilerReports") == "true") {
                freeCompilerArgs += listOf(
                    "-P",
                    "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=${project.layout.buildDirectory.asFile.get().absolutePath}/compose_compiler"
                )
            }
            if (project.findProperty("composeCompilerMetrics") == "true") {
                freeCompilerArgs += listOf(
                    "-P",
                    "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=${project.layout.buildDirectory.asFile.get().absolutePath}/compose_compiler"
                )
            }
        }
    }
}
