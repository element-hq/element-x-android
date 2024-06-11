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

/**
 * This will generate the plugin "io.element.android-compose-application" to use by app and samples modules
 */
import extension.androidConfig
import extension.commonDependencies
import extension.composeConfig
import extension.composeDependencies
import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

val libs = the<LibrariesForLibs>()
plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.autonomousapps.dependency-analysis")
}

android {
    androidConfig(project)
    composeConfig(libs)
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    commonDependencies(libs)
    composeDependencies(libs)
    coreLibraryDesugaring(libs.android.desugar)
}

tasks.withType(KotlinCompilationTask::class.java) {
    logger.warn("Configuring Kotlin compilation task $path:$name")
    doLast {
        val freeMemory = Runtime.getRuntime().freeMemory() / 1024 / 1024
        val maxMemory = Runtime.getRuntime().maxMemory() / 1024 / 1024
        val totalMemory = Runtime.getRuntime().totalMemory() / 1024 / 1024
        val usedMemory = totalMemory - freeMemory
        logger.warn("Memory usage: $usedMemory/$totalMemory (max: $maxMemory) MB")
        logger.warn("Free disk space: ${rootDir.freeSpace / 1024 / 1024} MB")
    }
}
