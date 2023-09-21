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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.designsystem.components.preferences.PreferenceSwitch
import io.element.android.libraries.designsystem.components.preferences.PreferenceView
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun AdvancedSettingsView(
    state: AdvancedSettingsState,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PreferenceView(
        modifier = modifier,
        onBackPressed = onBackPressed,
        title = stringResource(id = CommonStrings.common_advanced_settings)
    ) {
        PreferenceSwitch(
            title = stringResource(id = CommonStrings.common_rich_text_editor),
            // TODO i18n
            subtitle = "Disable the rich text editor to type Markdown manually",
            isChecked = state.isRichTextEditorEnabled,
            onCheckedChange = { state.eventSink(AdvancedSettingsEvents.SetRichTextEditorEnabled(it)) },
        )
        PreferenceSwitch(
            // TODO i18n
            title = "Developer mode",
            // TODO i18n
            subtitle = "The developer mode activates hidden features. For developers only!",
            isChecked = state.isDeveloperModeEnabled,
            onCheckedChange = { state.eventSink(AdvancedSettingsEvents.SetDeveloperModeEnabled(it)) },
        )
    }
}

@PreviewsDayNight
@Composable
internal fun AdvancedSettingsViewPreview(@PreviewParameter(AdvancedSettingsStateProvider::class) state: AdvancedSettingsState) =
    ElementPreview {
        AdvancedSettingsView(state = state, onBackPressed = { })
    }
