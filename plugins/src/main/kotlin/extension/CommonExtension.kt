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
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import isEnterpriseBuild
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension
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


fun Project.setupCompileOptions() {
    tasks.withType<KotlinCompile> {
        compilerOptions {
            // Warnings are potential errors, so stop ignoring them
            // This is disabled by default, but the CI will enforce this.
            // You can override by passing `-PallWarningsAsErrors=true` in the command line
            // Or add a line with "allWarningsAsErrors=true" in your ~/.gradle/gradle.properties file
            allWarningsAsErrors.set(providers.gradleProperty("allWarningsAsErrors").orNull == "true")

            // Uncomment to suppress Compose Kotlin compiler compatibility warning
//            freeCompilerArgs.addAll(listOf("-P", "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true"))

            // Fix compilation warning for annotations
            // See https://youtrack.jetbrains.com/issue/KT-73255/Change-defaulting-rule-for-annotations for more details
            freeCompilerArgs.add("-Xannotation-default-target=first-only")
        }
    }
}

fun Project.setupLintTasks() {
    // Detekt
    setupDetekt()

    // KtLint
    setupKtlint()

    // Dependency check
    plugins.apply("org.owasp.dependencycheck")

    tasks.register("runQualityChecks") {
        tasks.findByPath("$path:lintDebug")?.let { dependsOn(it) }
        tasks.findByName("detekt")?.let { dependsOn(it) }
        tasks.findByName("ktlintCheck")?.let { dependsOn(it) }
    }
}

fun Project.setupDetekt() {
    plugins.apply("io.gitlab.arturbosch.detekt")
    extensions.configure<DetektExtension> {
        buildUponDefaultConfig = true
        // activate all available (even unstable) rules.
        allRules = true
        // point to your custom config defining rules to run, overwriting default behavior
        config.from(files("$rootDir/tools/detekt/detekt.yml"))
    }

    val catalog = the<LibrariesForLibs>()
    dependencies.add("detektPlugins", catalog.detekt.compose.rules)
    dependencies.add("detektPlugins", project(":tests:detekt-rules"))

    tasks.withType<Detekt>().configureEach {
        exclude("io/element/android/tests/konsist/failures/**")

        // This file comes from another project and we want to keep it as close to the original as possible
        exclude("org/rustls/platformverifier/**")
    }
}

fun Project.setupKtlint() {
    plugins.apply("org.jlleitschuh.gradle.ktlint")

    // See https://github.com/JLLeitschuh/ktlint-gradle#configuration
    extensions.configure<KtlintExtension> {
        val catalog = the<LibrariesForLibs>()
        version.set(catalog.versions.ktlint.get())
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
}
