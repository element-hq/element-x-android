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
import io.element.android.features.preferences.api.store.PreferencesStore
import io.element.android.libraries.architecture.Presenter
import kotlinx.coroutines.launch
import javax.inject.Inject

class AdvancedSettingsPresenter @Inject constructor(
    private val preferencesStore: PreferencesStore,
) : Presenter<AdvancedSettingsState> {
    @Composable
    override fun present(): AdvancedSettingsState {
        val localCoroutineScope = rememberCoroutineScope()
        val isRichTextEditorEnabled by preferencesStore
            .isRichTextEditorEnabledFlow()
            .collectAsState(initial = false)
        val isDeveloperModeEnabled by preferencesStore
            .isDeveloperModeEnabledFlow()
            .collectAsState(initial = false)
        val isPrivateReadReceiptsEnabled by preferencesStore
            .isPrivateReadReceiptsEnabled()
            .collectAsState(initial = false)
        val theme by remember {
            preferencesStore.getThemeFlow().mapToTheme()
        }
            .collectAsState(initial = Theme.System)
        var showChangeThemeDialog by remember { mutableStateOf(false) }
        fun handleEvents(event: AdvancedSettingsEvents) {
            when (event) {
                is AdvancedSettingsEvents.SetRichTextEditorEnabled -> localCoroutineScope.launch {
                    preferencesStore.setRichTextEditorEnabled(event.enabled)
                }
                is AdvancedSettingsEvents.SetDeveloperModeEnabled -> localCoroutineScope.launch {
                    preferencesStore.setDeveloperModeEnabled(event.enabled)
                }
                is AdvancedSettingsEvents.SetPrivateReadReceiptsEnabled -> localCoroutineScope.launch {
                    preferencesStore.setPrivateReadReceiptsEnabled(event.enabled)
                }
                AdvancedSettingsEvents.CancelChangeTheme -> showChangeThemeDialog = false
                AdvancedSettingsEvents.ChangeTheme -> showChangeThemeDialog = true
                is AdvancedSettingsEvents.SetTheme -> localCoroutineScope.launch {
                    preferencesStore.setTheme(event.theme.name)
                    showChangeThemeDialog = false
                }
            }
        }

        return AdvancedSettingsState(
            isRichTextEditorEnabled = isRichTextEditorEnabled,
            isDeveloperModeEnabled = isDeveloperModeEnabled,
            isPrivateReadReceiptsEnabled = isPrivateReadReceiptsEnabled,
            theme = theme,
            showChangeThemeDialog = showChangeThemeDialog,
            eventSink = { handleEvents(it) }
        )
    }
}
