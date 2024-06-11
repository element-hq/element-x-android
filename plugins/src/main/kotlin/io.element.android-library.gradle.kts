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
 * This will generate the plugin "io.element.android-library", used in android library without compose modules.
 */
import extension.androidConfig
import extension.commonDependencies
import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

val libs = the<LibrariesForLibs>()
plugins {
    id("com.android.library")
    id("kotlin-android")
    id("com.autonomousapps.dependency-analysis")
}

android {
    androidConfig(project)
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    commonDependencies(libs)
    coreLibraryDesugaring(libs.android.desugar)
}

tasks.register("logMemoryUsage") {
    doFirst {
        val freeMemory = Runtime.getRuntime().freeMemory() / 1024 / 1024
        val maxMemory = Runtime.getRuntime().maxMemory() / 1024 / 1024
        val totalMemory = Runtime.getRuntime().totalMemory() / 1024 / 1024
        val usedMemory = totalMemory - freeMemory
        logger.warn("Memory usage: $usedMemory/$maxMemory MB")
    }
}

tasks.withType(KotlinCompilationTask::class.java) {
    logger.warn("Configuring Kotlin compilation task $path:$name")
    finalizedBy("logMemoryUsage")
}
