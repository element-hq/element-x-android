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
import androidx.compose.runtime.rememberCoroutineScope
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
            .isDevelopModeEnabledFlow()
            .collectAsState(initial = false)

        fun handleEvents(event: AdvancedSettingsEvents) {
            when (event) {
                is AdvancedSettingsEvents.SetRichTextEditorEnabled -> localCoroutineScope.launch {
                    preferencesStore.setRichTextEditorEnabled(event.enabled)
                }
                is AdvancedSettingsEvents.SetDeveloperModeEnabled -> localCoroutineScope.launch {
                    preferencesStore.setDevelopModeEnabled(event.enabled)
                }
            }
        }

        return AdvancedSettingsState(
            isRichTextEditorEnabled = isRichTextEditorEnabled,
            isDeveloperModeEnabled = isDeveloperModeEnabled,
            eventSink = ::handleEvents
        )
    }
}
