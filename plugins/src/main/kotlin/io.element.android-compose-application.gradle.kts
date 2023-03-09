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

val libs = the<LibrariesForLibs>()

plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    androidConfig(project)
    composeConfig(libs)
}

dependencies {
    commonDependencies(libs)
    composeDependencies(libs)
}
