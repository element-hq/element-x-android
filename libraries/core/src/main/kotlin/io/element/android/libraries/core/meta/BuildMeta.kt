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

package io.element.android.libraries.core.meta

data class BuildMeta(
    val buildType: BuildType,
    val isDebuggable: Boolean,
    val applicationName: String,
    val desktopApplicationName: String,
    val applicationId: String,
    val lowPrivacyLoggingEnabled: Boolean,
    val versionName: String,
    val versionCode: Int,
    val gitRevision: String,
    val gitBranchName: String,
    val flavorDescription: String,
    val flavorShortDescription: String,
)
