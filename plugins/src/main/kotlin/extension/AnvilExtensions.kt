/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
 * @param generateDaggerCode whether to enable general Dagger code generation using Kapt. `false` by default.
 * @param generateDaggerFactoriesUsingAnvil whether to generate Dagger factories using Anvil instead of Kapt. `true` by default.
 * @param componentMergingStrategy how to perform component merging. This is `ComponentMergingStrategy.NONE` by default, which will prevent component merging
 * from running.
 */
fun Project.setupAnvil(
    generateDaggerCode: Boolean = false,
    generateDaggerFactoriesUsingAnvil: Boolean = true,
    componentMergingStrategy: ComponentMergingStrategy = ComponentMergingStrategy.NONE,
) {
    val libs = the<LibrariesForLibs>()

    // Add dagger dependency, needed for generated code
    dependencies.implementation(libs.dagger)

    // Apply Anvil plugin and configure it
    applyPluginIfNeeded(libs.plugins.anvil)

    project.pluginManager.withPlugin(libs.plugins.anvil.get().pluginId) {
        // Setup extension
        extensions.configure(AnvilExtension::class.java) {
            this.generateDaggerFactories.set(generateDaggerFactoriesUsingAnvil)
            this.disableComponentMerging.set(componentMergingStrategy == ComponentMergingStrategy.NONE)

            useKsp(
                contributesAndFactoryGeneration = true,
                componentMerging = componentMergingStrategy == ComponentMergingStrategy.KSP,
            )
        }
    }

    if (generateDaggerCode) {
        // Needed at the top level since dagger code should be generated at a single point for performance reasons
        dependencies.add("ksp", libs.dagger.compiler)
    }

    // These dependencies are only needed for compose library or application modules
    if (project.pluginManager.hasPlugin("io.element.android-compose-library")
        || project.pluginManager.hasPlugin("io.element.android-compose-application")) {
        // Annotations to generate DI code for Appyx nodes
        dependencies.implementation(project.project(":anvilannotations"))
        // Code generator for the annotations above
        dependencies.add("ksp", project.project(":anvilcodegen"))
    }
}

private fun Project.applyPluginIfNeeded(plugin: Provider<PluginDependency>) {
    val pluginId = plugin.get().pluginId
    if (!pluginManager.hasPlugin(pluginId)) {
        pluginManager.apply(pluginId)
    }
}

enum class ComponentMergingStrategy {
    NONE,
    KAPT,
    KSP
}
