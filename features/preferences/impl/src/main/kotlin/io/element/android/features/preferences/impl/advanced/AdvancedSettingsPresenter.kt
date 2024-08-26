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

package io.element.android.features.preferences.impl.advanced

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.element.android.compound.theme.Theme
import io.element.android.compound.theme.mapToTheme
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import kotlinx.coroutines.launch
import javax.inject.Inject

class AdvancedSettingsPresenter @Inject constructor(
    private val appPreferencesStore: AppPreferencesStore,
    private val sessionPreferencesStore: SessionPreferencesStore,
) : Presenter<AdvancedSettingsState> {
    @Composable
    override fun present(): AdvancedSettingsState {
        val localCoroutineScope = rememberCoroutineScope()
        val isDeveloperModeEnabled by appPreferencesStore
            .isDeveloperModeEnabledFlow()
            .collectAsState(initial = false)
        val isSharePresenceEnabled by sessionPreferencesStore
            .isSharePresenceEnabled()
            .collectAsState(initial = true)
        val skinTone by sessionPreferencesStore
            .getSkinTone()
            .collectAsState(initial = null)
        val theme by remember {
            appPreferencesStore.getThemeFlow().mapToTheme()
        }
            .collectAsState(initial = Theme.System)
        var showChangeSkinToneDialog by remember { mutableStateOf(false) }
        var showChangeThemeDialog by remember { mutableStateOf(false) }

        fun handleEvents(event: AdvancedSettingsEvents) {
            when (event) {
                is AdvancedSettingsEvents.SetDeveloperModeEnabled -> localCoroutineScope.launch {
                    appPreferencesStore.setDeveloperModeEnabled(event.enabled)
                }
                is AdvancedSettingsEvents.SetSharePresenceEnabled -> localCoroutineScope.launch {
                    sessionPreferencesStore.setSharePresence(event.enabled)
                }
                AdvancedSettingsEvents.CancelChangeTheme -> showChangeThemeDialog = false
                AdvancedSettingsEvents.ChangeTheme -> showChangeThemeDialog = true
                is AdvancedSettingsEvents.SetTheme -> localCoroutineScope.launch {
                    appPreferencesStore.setTheme(event.theme.name)
                    showChangeThemeDialog = false
                }
                AdvancedSettingsEvents.CancelChangeSkinTone -> showChangeSkinToneDialog = false
                AdvancedSettingsEvents.ChangeSkinTone -> showChangeSkinToneDialog = true
                is AdvancedSettingsEvents.SetSkinTone -> localCoroutineScope.launch {
                    sessionPreferencesStore.setSkinTone(event.modifier)
                    showChangeSkinToneDialog = false
                }
            }
        }

        return AdvancedSettingsState(
            isDeveloperModeEnabled = isDeveloperModeEnabled,
            isSharePresenceEnabled = isSharePresenceEnabled,
            theme = theme,
            showChangeThemeDialog = showChangeThemeDialog,
            skinTone = skinTone,
            showChangeSkinToneDialog = showChangeSkinToneDialog,
            eventSink = { handleEvents(it) }
        )
    }
}
