/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package extension

import org.gradle.api.Project
import com.squareup.anvil.plugin.AnvilExtension

/**
 * Setup Anvil plugin with the given configuration.
 * @param generateDaggerFactories whether to generate Dagger factories using Anvil
 */
fun Project.setupAnvil(generateDaggerFactories: Boolean = true) {
    plugins.apply("com.squareup.anvil")
    project.pluginManager.withPlugin("com.squareup.anvil") {
        extensions.configure(AnvilExtension::class.java) {
            this.generateDaggerFactories.set(generateDaggerFactories)

            // These dependencies are only needed for compose libraries
            if (project.pluginManager.hasPlugin("io.element.android-compose-library")) {
                // Annotations to generate DI code for Appyx nodes
                dependencies.add("implementation", project.project(":anvilannotations"))
                // Code generator for the annotations above
                dependencies.add("anvil", project.project(":anvilcodegen"))
            }
        }
    }
}
