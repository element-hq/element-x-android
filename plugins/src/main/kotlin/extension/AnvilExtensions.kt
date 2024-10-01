/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package extension

import com.squareup.anvil.plugin.AnvilExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.the
import org.gradle.plugin.use.PluginDependency

/**
 * Setup Anvil plugin with the given configuration.
 * @param generateDaggerCode whether to enable general Dagger code generation using Kapt
 * @param generateDaggerFactoriesUsingAnvil whether to generate Dagger factories using Anvil instead of Kapt
 */
fun Project.setupAnvil(
    generateDaggerCode: Boolean = false,
    generateDaggerFactoriesUsingAnvil: Boolean = true,
) {
    val libs = the<LibrariesForLibs>()
    // Apply plugins and dependencies
    applyPluginIfNeeded(libs.plugins.anvil)

    if (generateDaggerCode) {
        applyPluginIfNeeded(libs.plugins.kapt)
        // Needed at the top level since dagger code should be generated at a single point for performance
        dependencies.implementation(libs.dagger)
        dependencies.add("kapt", libs.dagger.compiler)
    }

    // These dependencies are only needed for compose library or application modules
    if (project.pluginManager.hasPlugin("io.element.android-compose-library")
        || project.pluginManager.hasPlugin("io.element.android-compose-application")) {
        // Annotations to generate DI code for Appyx nodes
        dependencies.implementation(project.project(":anvilannotations"))
        // Code generator for the annotations above
        dependencies.add("anvil", project.project(":anvilcodegen"))
    }

    project.pluginManager.withPlugin(libs.plugins.anvil.get().pluginId) {
        // Setup extension
        extensions.configure(AnvilExtension::class.java) {
            this.generateDaggerFactories.set(generateDaggerFactoriesUsingAnvil)
        }
    }
}

private fun Project.applyPluginIfNeeded(plugin: Provider<PluginDependency>) {
    val pluginId = plugin.get().pluginId
    if (!pluginManager.hasPlugin(pluginId)) {
        pluginManager.apply(pluginId)
    }
}
