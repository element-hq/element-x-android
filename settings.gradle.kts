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
        // Declared first and filtered so that Android plugins are not looked up in the other repositories, and the other
        // plugins are not looked up in Google's Maven repository.
        google {
            content {
                includeGroupByRegex("android\\..*")
                includeGroupByRegex("androidx\\..*")
                includeGroupByRegex("com\\.android(\\..*)?")
                includeGroupByRegex("com\\.google\\.firebase.*")
                includeGroupByRegex("com\\.google\\.android.*")
                includeGroupByRegex("com\\.google\\.gms.*")
                includeGroupByRegex("com\\.google\\.testing\\.platform.*")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        exclusiveContent {
            forRepository {
                maven {
                    url = uri("https://github.com/rustls/rustls-platform-verifier/raw/maven-archive/android-release-support/maven/")
                }
            }
            filter {
                includeModule("org.rustls", "rustls-platform-verifier")
            }
        }
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
                includeGroupByRegex("com\\.android(\\..*)?")
                includeGroupByRegex("com\\.google\\.firebase.*")
                includeGroupByRegex("com\\.google\\.android.*")
                includeGroupByRegex("com\\.google\\.gms.*")
                includeGroupByRegex("com\\.google\\.testing\\.platform.*")
            }
        }
        mavenCentral()
        // This is also Maven Central. Restrict it to our own dependencies, that we may need right after their release, so
        // dependencies missing from Maven Central are not requested twice to it.
        maven {
            url = uri("https://repo1.maven.org/maven2/")
            content {
                includeModule("org.matrix.rustcomponents", "sdk-android")
                includeGroup("io.element.android")
            }
        }
        flatDir {
            dirs("libraries/matrix/libs")
        }

        // Add ivy repository for nightly builds of the SDK bindings for Android, which are published as GitHub release assets.
        // Exclusive so the nightly versions are only looked up in this repository and never in Maven Central, while the release
        // versions are never looked up in this repository.
        exclusiveContent {
            forRepository {
                ivy {
                    url = uri("https://github.com/matrix-org/matrix-rust-components-kotlin/releases/download")
                    patternLayout { artifact("sdk-[revision]/[artifact]-[revision](-[classifier])(.[ext])") }
                    metadataSources { gradleMetadata() }
                }
            }
            filter {
                // The nightly versions of the SDK bindings for Android
                includeVersionByRegex("org\\.matrix\\.rustcomponents", "sdk-android", ".*-nightly")
            }
        }

        // Testing an unreleased element-call-android build. Publish it there with
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
include(":tests:screenshotutils")
include(":tests:uitests")
include(":tests:testutils")
include(":annotations")
include(":codegen")

/**
 * Finds the Gradle projects in [directory]: the folders containing a `build.gradle.kts` file, up to [maxDepth] levels below.
 *
 * The search runs in a [ValueSource] so that only its result, the list of projects, is a configuration cache input. Listing
 * the folders directly in the settings script would make each listed folder an input, and adding or removing any file in
 * them (build outputs, `.git` content, etc.) would invalidate the configuration cache.
 */
private fun findProjects(directory: File, path: String, maxDepth: Int = 1): List<String> {
    return providers.of(ProjectsValueSource::class) {
        parameters {
            this.directory.set(directory)
            this.path.set(path)
            this.maxDepth.set(maxDepth)
        }
    }.get()
}

abstract class ProjectsValueSource : ValueSource<List<String>, ProjectsValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        val directory: Property<File>
        val path: Property<String>
        val maxDepth: Property<Int>
    }

    override fun obtain(): List<String> = search(parameters.directory.get(), parameters.path.get(), parameters.maxDepth.get())

    private fun search(directory: File, path: String, maxDepth: Int): List<String> {
        return directory.listFiles { file ->
            // Skip hidden folders (e.g. `.git`) and build outputs, which can't contain projects.
            file.isDirectory && !file.name.startsWith(".") && file.name != "build"
        }.orEmpty().sorted().flatMap { subDir ->
            val newPath = "$path:${subDir.name}"
            when {
                File(subDir, "build.gradle.kts").exists() -> listOf(newPath)
                maxDepth > 0 -> search(subDir, newPath, maxDepth - 1)
                else -> emptyList()
            }
        }
    }
}

private fun includeProjects(projectPaths: List<String>) {
    projectPaths.forEach { projectPath ->
        include(projectPath)
        logger.info("Included project: $projectPath")
    }
}

private fun includeProjects(directory: File, path: String, maxDepth: Int = 1) = includeProjects(findProjects(directory, path, maxDepth))

// Enterprise build detection. By default, the build is an Enterprise build if the enterprise submodule contains
// some Gradle projects. This can be overridden with the Gradle property `element.enterprise` (true or false),
// for instance to build the FOSS version while the submodule is checked out: `./gradlew -Pelement.enterprise=false ...`
private val enterpriseProjects = findProjects(File(rootDir, "enterprise"), ":enterprise", maxDepth = 2)
private val isEnterpriseBuild = providers.gradleProperty("element.enterprise").orNull?.toBooleanStrict() ?: enterpriseProjects.isNotEmpty()
if (isEnterpriseBuild) {
    check(enterpriseProjects.isNotEmpty()) {
        "Enterprise build requested but no enterprise sources found. Run `git submodule update --init enterprise`."
    }
    includeProjects(enterpriseProjects)
}
// Read by `Project.isEnterpriseBuild` in the plugins module.
gradle.extra["isEnterpriseBuild"] = isEnterpriseBuild

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
