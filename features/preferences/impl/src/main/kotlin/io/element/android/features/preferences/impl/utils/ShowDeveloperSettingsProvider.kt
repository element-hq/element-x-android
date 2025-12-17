/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.utils

import dev.zacsweers.metro.Inject
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.ui.utils.MultipleTapToUnlock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Inject
class ShowDeveloperSettingsProvider(
    buildMeta: BuildMeta,
) {
    companion object {
        const val DEVELOPER_SETTINGS_COUNTER = 7
    }

    private val multipleTapToUnlock = MultipleTapToUnlock(DEVELOPER_SETTINGS_COUNTER)
    private val isDeveloperBuild = buildMeta.buildType != BuildType.RELEASE

    private val _showDeveloperSettings = MutableStateFlow(isDeveloperBuild)
    val showDeveloperSettings: StateFlow<Boolean> = _showDeveloperSettings

    fun unlockDeveloperSettings(scope: CoroutineScope) {
        if (multipleTapToUnlock.unlock(scope)) {
            _showDeveloperSettings.value = true
        }
    }
}
