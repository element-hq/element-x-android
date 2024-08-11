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

package extension

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.logging.Logger
import org.gradle.internal.cc.base.logger
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.closureOf
import org.gradle.kotlin.dsl.project
import java.io.File
import kotlin.system.measureTimeMillis

private fun DependencyHandlerScope.implementation(dependency: Any) = dependencies.add("implementation", dependency)

// Implementation + config block
private fun DependencyHandlerScope.implementation(
    dependency: Any,
    config: Action<ExternalModuleDependency>
) = dependencies.add("implementation",  dependency, closureOf<ExternalModuleDependency> { config.execute(this) })

private fun DependencyHandlerScope.androidTestImplementation(dependency: Any) = dependencies.add("androidTestImplementation", dependency)

private fun DependencyHandlerScope.debugImplementation(dependency: Any) = dependencies.add("debugImplementation", dependency)

/**
 * Dependencies used by all the modules
 */
fun DependencyHandlerScope.commonDependencies(libs: LibrariesForLibs) {
    implementation(libs.timber)
}

/**
 * Dependencies used by all the modules with composable items
 */
fun DependencyHandlerScope.composeDependencies(libs: LibrariesForLibs) {
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.showkase)
    implementation(libs.kotlinx.collections.immutable)
}

fun DependencyHandlerScope.allLibrariesImpl() {
    implementation(project(":libraries:androidutils"))
    implementation(project(":libraries:deeplink"))
    implementation(project(":libraries:designsystem"))
    implementation(project(":libraries:matrix:impl"))
    implementation(project(":libraries:matrixui"))
    implementation(project(":libraries:network"))
    implementation(project(":libraries:core"))
    implementation(project(":libraries:eventformatter:impl"))
    implementation(project(":libraries:indicator:impl"))
    implementation(project(":libraries:permissions:impl"))
    implementation(project(":libraries:push:impl"))
    implementation(project(":libraries:push:impl"))
    implementation(project(":libraries:featureflag:impl"))
    implementation(project(":libraries:pushstore:impl"))
    implementation(project(":libraries:preferences:impl"))
    implementation(project(":libraries:architecture"))
    implementation(project(":libraries:dateformatter:impl"))
    implementation(project(":libraries:di"))
    implementation(project(":libraries:session-storage:impl"))
    implementation(project(":libraries:mediapickers:impl"))
    implementation(project(":libraries:mediaupload:impl"))
    implementation(project(":libraries:usersearch:impl"))
    implementation(project(":libraries:textcomposer:impl"))
    implementation(project(":libraries:roomselect:impl"))
    implementation(project(":libraries:cryptography:impl"))
    implementation(project(":libraries:voicerecorder:impl"))
    implementation(project(":libraries:mediaplayer:impl"))
    implementation(project(":libraries:mediaviewer:impl"))
    implementation(project(":libraries:troubleshoot:impl"))
    implementation(project(":libraries:fullscreenintent:impl"))
}

fun DependencyHandlerScope.allServicesImpl() {
    // For analytics configuration, either use noop, or use the impl, with at least one analyticsproviders implementation
    // implementation(project(":services:analytics:noop"))
    implementation(project(":services:analytics:impl"))
    implementation(project(":services:analyticsproviders:posthog"))
    implementation(project(":services:analyticsproviders:sentry"))

    implementation(project(":services:apperror:impl"))
    implementation(project(":services:appnavstate:impl"))
    implementation(project(":services:toolbox:impl"))
}

fun DependencyHandlerScope.allEnterpriseImpl(project: Project) = addAll(project, logger, "enterprise", "impl")

fun DependencyHandlerScope.allFeaturesImpl(project: Project) = addAll(project, logger, "features", "impl")

fun DependencyHandlerScope.allFeaturesApi(project: Project) = addAll(project, logger, "features", "api")

private fun DependencyHandlerScope.addAll(project: Project, logger: Logger, prefix: String, suffix: String) {
    val subProjects = project.rootProject.subprojects.filter { it.path.startsWith(":$prefix") && it.path.endsWith(":$suffix") }
    logger.lifecycle("Found ${subProjects.size} $suffix modules in $prefix")
    for (p in subProjects) {
        add("implementation", p)
    }
}
