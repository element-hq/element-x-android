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

package io.element.android.libraries.featureflag.test

import io.element.android.features.preferences.api.store.PreferencesStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class InMemoryPreferencesStore(
    isRichTextEditorEnabled: Boolean = false,
    isDeveloperModeEnabled: Boolean = false,
    customElementCallBaseUrl: String? = null,
) : PreferencesStore {
    private var _isRichTextEditorEnabled = MutableStateFlow(isRichTextEditorEnabled)
    private var _isDeveloperModeEnabled = MutableStateFlow(isDeveloperModeEnabled)
    private var _customElementCallBaseUrl = MutableStateFlow(customElementCallBaseUrl)

    override suspend fun setRichTextEditorEnabled(enabled: Boolean) {
        _isRichTextEditorEnabled.value = enabled
    }

    override fun isRichTextEditorEnabledFlow(): Flow<Boolean> {
        return _isRichTextEditorEnabled
    }

    override suspend fun setDeveloperModeEnabled(enabled: Boolean) {
        _isDeveloperModeEnabled.value = enabled
    }

    override fun isDeveloperModeEnabledFlow(): Flow<Boolean> {
        return _isDeveloperModeEnabled
    }

    override suspend fun setCustomElementCallBaseUrl(string: String?) {
        _customElementCallBaseUrl.tryEmit(string)
    }

    override fun getCustomElementCallBaseUrlFlow(): Flow<String?> {
        return _customElementCallBaseUrl
    }

    override suspend fun reset() {
        // No op
    }
}
