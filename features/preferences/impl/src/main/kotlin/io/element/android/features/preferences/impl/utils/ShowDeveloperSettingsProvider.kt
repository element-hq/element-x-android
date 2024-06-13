/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.preferences.impl.utils

import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ShowDeveloperSettingsProvider @Inject constructor(
    buildMeta: BuildMeta,
) {
    companion object {
        const val DEVELOPER_SETTINGS_COUNTER = 7
    }
    private var counter = DEVELOPER_SETTINGS_COUNTER
    private val isDeveloperBuild = buildMeta.buildType != BuildType.RELEASE

    private val _showDeveloperSettings = MutableStateFlow(isDeveloperBuild)
    val showDeveloperSettings: StateFlow<Boolean> = _showDeveloperSettings

    fun unlockDeveloperSettings() {
        if (counter == 0) {
            return
        }
        counter--
        if (counter == 0) {
            _showDeveloperSettings.value = true
        }
    }
}
