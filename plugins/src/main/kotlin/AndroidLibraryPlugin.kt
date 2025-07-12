/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

/**
 * This will generate the plugin "io.element.android.library", used in android library without compose modules.
 */
import extension.androidConfig
import extension.commonDependencies
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidLibraryPlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(target: org.gradle.api.Project) {
        with(target) {
            plugins.apply("com.android.library")
            plugins.apply("kotlin-android")
            plugins.apply("com.autonomousapps.dependency-analysis")

            extensions.configure<com.android.build.gradle.LibraryExtension>("android") {
                androidConfig(project)
                compileOptions {
                    isCoreLibraryDesugaringEnabled = true
                }
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinAndroidExtension>("kotlin") {
                jvmToolchain {
                    languageVersion.set(Versions.javaLanguageVersion)
                }
            }

            dependencies {
                commonDependencies(libs)
                add("coreLibraryDesugaring", libs.findLibrary("android.desugar").get())
            }
        }
    }
}
