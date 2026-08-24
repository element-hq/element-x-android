/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.utils

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.ui.utils.MultipleTapToUnlock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@SingleIn(AppScope::class)
@Inject
class ShowDeveloperSettingsProvider(
    buildMeta: BuildMeta,
    private val appPreferencesStore: AppPreferencesStore,
    @AppCoroutineScope
    private val appCoroutineScope: CoroutineScope,
) {
    companion object {
        const val DEVELOPER_SETTINGS_COUNTER = 7
    }

    private var multipleTapToUnlock = MultipleTapToUnlock(DEVELOPER_SETTINGS_COUNTER)

    val showDeveloperSettings: StateFlow<Boolean> = appPreferencesStore
        .showDeveloperSettingsFlow()
        .stateIn(
            scope = appCoroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = buildMeta.buildType != BuildType.RELEASE,
        )

    fun unlockDeveloperSettings(scope: CoroutineScope) {
        if (multipleTapToUnlock.unlock(scope)) {
            multipleTapToUnlock = MultipleTapToUnlock(DEVELOPER_SETTINGS_COUNTER)
            setShowDeveloperSettings(true)
        }
    }

    fun setShowDeveloperSettings(show: Boolean) {
        appCoroutineScope.launch {
            appPreferencesStore.setShowDeveloperSettings(show)
        }
    }
}
