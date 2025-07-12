/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

/**
 * This will generate the plugin "io.element.android-compose-application" to use by app
 */
import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.LibraryExtension
import extension.androidConfig
import extension.commonDependencies
import extension.composeConfig
import extension.composeDependencies
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidExtension

class AndroidComposeApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            plugins.apply("com.android.application")
            plugins.apply("kotlin-android")
            plugins.apply("org.jetbrains.kotlin.plugin.compose")

            extensions.configure<CommonExtension<*, *, *, *, *, *>>("android") {
                androidConfig(project)
                composeConfig()
                compileOptions {
                    isCoreLibraryDesugaringEnabled = true
                }
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            extensions.configure<KotlinAndroidExtension>("kotlin") {
                jvmToolchain {
                    languageVersion.set(Versions.javaLanguageVersion)
                }
            }

            dependencies {
                commonDependencies(libs)
                composeDependencies(libs)
                add("coreLibraryDesugaring", libs.findLibrary("android.desugar").get())
            }
        }
    }
}
