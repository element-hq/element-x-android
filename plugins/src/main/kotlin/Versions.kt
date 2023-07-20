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

import org.gradle.api.JavaVersion
import org.gradle.jvm.toolchain.JavaLanguageVersion

// Note: 2 digits max for each value
private const val versionMajor = 0
private const val versionMinor = 1

// Note: even values are reserved for regular release, odd values for hotfix release.
// When creating a hotfix, you should decrease the value, since the current value
// is the value for the next regular release.
private const val versionPatch = 2

object Versions {
    val versionCode = (versionMajor * 1_00_00 + versionMinor * 1_00 + versionPatch) * 10
    val versionName = "$versionMajor.$versionMinor.$versionPatch"
    const val compileSdk = 33
    const val targetSdk = 33
    const val minSdk = 23
    val javaCompileVersion = JavaVersion.VERSION_17
    val javaLanguageVersion: JavaLanguageVersion = JavaLanguageVersion.of(11)
}
