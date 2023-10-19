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

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class AdvancedSettingsStateProvider : PreviewParameterProvider<AdvancedSettingsState> {
    override val values: Sequence<AdvancedSettingsState>
        get() = sequenceOf(
            aAdvancedSettingsState(),
            aAdvancedSettingsState(isRichTextEditorEnabled = true),
            aAdvancedSettingsState(isDeveloperModeEnabled = true),
            aAdvancedSettingsState(customElementCallBaseUrl = "https://call.element.io"),
        )
}

fun aAdvancedSettingsState(
    isRichTextEditorEnabled: Boolean = false,
    isDeveloperModeEnabled: Boolean = false,
    customElementCallBaseUrl: String? = null,
) = AdvancedSettingsState(
    isRichTextEditorEnabled = isRichTextEditorEnabled,
    isDeveloperModeEnabled = isDeveloperModeEnabled,
    customElementCallBaseUrlState = customElementCallBaseUrl?.let { CustomElementCallBaseUrlState(it, "https://call.element.io") { true } },
    eventSink = {}
)
