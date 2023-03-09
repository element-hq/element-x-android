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

import gradle.kotlin.dsl.accessors._71f190358cebd46a469f2989484fd643.androidTestImplementation
import gradle.kotlin.dsl.accessors._71f190358cebd46a469f2989484fd643.debugImplementation
import gradle.kotlin.dsl.accessors._71f190358cebd46a469f2989484fd643.implementation
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.project

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
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material:material")
    // Override BOM version, SearchBar is not available in the actual version
    // do not use latest version because of clashes on androidx lifecycle dependency
    implementation("androidx.compose.material3:material3:1.1.0-alpha04")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation(libs.androidx.activity.compose)
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation(libs.showkase)
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")
}

fun DependencyHandlerScope.allLibraries() {
    implementation(project(":libraries:designsystem"))
    implementation(project(":libraries:matrix:api"))
    implementation(project(":libraries:matrixui"))
    implementation(project(":libraries:core"))
    implementation(project(":libraries:architecture"))
    implementation(project(":libraries:dateformatter:api"))
    implementation(project(":libraries:di"))
}

fun DependencyHandlerScope.allFeatures() {
    implementation(project(":features:onboarding"))
    implementation(project(":features:login"))
    implementation(project(":features:logout"))
    implementation(project(":features:roomlist"))
    implementation(project(":features:messages"))
    implementation(project(":features:rageshake"))
    implementation(project(":features:preferences"))
    implementation(project(":features:createroom"))
}
