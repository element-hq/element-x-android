/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
