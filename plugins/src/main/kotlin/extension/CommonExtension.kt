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

import Versions
import com.android.build.api.dsl.CommonExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import java.io.File

fun CommonExtension<*, *, *, *, *, *>.androidConfig(project: Project) {
    defaultConfig {
        compileSdk = Versions.compileSdk
        minSdk = Versions.minSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
            generatedDensities()
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    lint {
        lintConfig = File("${project.rootDir}/tools/lint/lint.xml")
        checkDependencies = true
        abortOnError = true
        ignoreTestFixturesSources = true
        checkGeneratedSources = false
    }
}

fun CommonExtension<*, *, *, *, *, *>.composeConfig(libs: LibrariesForLibs) {

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.kotlin.get()
    }

    packaging {
        resources.excludes.apply {
            add("META-INF/AL2.0")
            add("META-INF/LGPL2.1")
        }
    }

    lint {
        // Extra rules for compose
        // Disabled until lint stops inspecting generated ksp files...
        // error.add("ComposableLambdaParameterNaming")
        error.add("ComposableLambdaParameterPosition")
        ignoreTestFixturesSources = true
        checkGeneratedSources = false
    }
}

