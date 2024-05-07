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

package io.element.android.libraries.preferences.test

import io.element.android.features.preferences.api.store.AppPreferencesStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class InMemoryAppPreferencesStore(
    isRichTextEditorEnabled: Boolean = false,
    isDeveloperModeEnabled: Boolean = false,
    customElementCallBaseUrl: String? = null,
    theme: String? = null,
) : AppPreferencesStore {
    private val isRichTextEditorEnabled = MutableStateFlow(isRichTextEditorEnabled)
    private val isDeveloperModeEnabled = MutableStateFlow(isDeveloperModeEnabled)
    private val customElementCallBaseUrl = MutableStateFlow(customElementCallBaseUrl)
    private val theme = MutableStateFlow(theme)

    override suspend fun setRichTextEditorEnabled(enabled: Boolean) {
        isRichTextEditorEnabled.value = enabled
    }

    override fun isRichTextEditorEnabledFlow(): Flow<Boolean> {
        return isRichTextEditorEnabled
    }

    override suspend fun setDeveloperModeEnabled(enabled: Boolean) {
        isDeveloperModeEnabled.value = enabled
    }

    override fun isDeveloperModeEnabledFlow(): Flow<Boolean> {
        return isDeveloperModeEnabled
    }

    override suspend fun setCustomElementCallBaseUrl(string: String?) {
        customElementCallBaseUrl.tryEmit(string)
    }

    override fun getCustomElementCallBaseUrlFlow(): Flow<String?> {
        return customElementCallBaseUrl
    }

    override suspend fun setTheme(theme: String) {
        this.theme.value = theme
    }

    override fun getThemeFlow(): Flow<String?> {
        return theme
    }

    override suspend fun reset() {
        // No op
    }
}
