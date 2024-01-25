/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.libraries.matrix.test.core

import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType

fun aBuildMeta(
    buildType: BuildType = BuildType.DEBUG,
    isDebuggable: Boolean = true,
    applicationName: String = "",
    applicationId: String = "",
    lowPrivacyLoggingEnabled: Boolean = true,
    versionName: String = "",
    versionCode: Int = 0,
    gitRevision: String = "",
    gitBranchName: String = "",
    flavorDescription: String = "",
    flavorShortDescription: String = "",
) = BuildMeta(
    buildType,
    isDebuggable,
    applicationName,
    applicationId,
    lowPrivacyLoggingEnabled,
    versionName,
    versionCode,
    gitRevision,
    gitBranchName,
    flavorDescription,
    flavorShortDescription
)
