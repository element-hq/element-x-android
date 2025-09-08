/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package extension

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.the
import org.gradle.plugin.use.PluginDependency

/**
 * Setup the Metro plugin with the shared configuration.
 * @param generateNodeFactories Whether to set up the KSP plugin and dependencies to generate Appyx Node factories.
 */
fun Project.setupDependencyInjection(
    generateNodeFactories: Boolean = shouldApplyAppyxCodegen(),
) {
    val libs = the<LibrariesForLibs>()

    // Apply Metro plugin and configure it
    applyPluginIfNeeded(libs.plugins.metro)

    if (generateNodeFactories) {
        applyPluginIfNeeded(libs.plugins.ksp)

        // Annotations to generate DI code for Appyx nodes
        dependencies.implementation(project.project(":annotations"))
        // Code generator for the annotations above
        dependencies.add("ksp", project.project(":codegen"))
    }
}

// These dependencies should only be needed for compose library or application modules
private fun Project.shouldApplyAppyxCodegen(): Boolean {
    return project.pluginManager.hasPlugin("io.element.android-compose-library")
        || project.pluginManager.hasPlugin("io.element.android-compose-application")
}

private fun Project.applyPluginIfNeeded(plugin: Provider<PluginDependency>) {
    val pluginId = plugin.get().pluginId
    if (!pluginManager.hasPlugin(pluginId)) {
        pluginManager.apply(pluginId)
    }
}
