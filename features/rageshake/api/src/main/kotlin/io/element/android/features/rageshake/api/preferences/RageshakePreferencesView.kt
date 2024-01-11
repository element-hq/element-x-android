/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.features.rageshake.api.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.rageshake.api.R
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.components.preferences.PreferenceSlide
import io.element.android.libraries.designsystem.components.preferences.PreferenceSwitch
import io.element.android.libraries.designsystem.components.preferences.PreferenceText
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun RageshakePreferencesView(
    state: RageshakePreferencesState,
    modifier: Modifier = Modifier,
) {
    fun onSensitivityChanged(sensitivity: Float) {
        state.eventSink(RageshakePreferencesEvents.SetSensitivity(sensitivity = sensitivity))
    }

    fun onEnabledChanged(isEnabled: Boolean) {
        state.eventSink(RageshakePreferencesEvents.SetIsEnabled(isEnabled = isEnabled))
    }

    Column(modifier = modifier) {
        PreferenceCategory(title = stringResource(id = R.string.settings_rageshake)) {
            if (state.isSupported) {
                PreferenceSwitch(
                    title = stringResource(id = CommonStrings.preference_rageshake),
                    isChecked = state.isEnabled,
                    onCheckedChange = ::onEnabledChanged
                )
                PreferenceSlide(
                    title = stringResource(id = R.string.settings_rageshake_detection_threshold),
                    // summary = stringResource(id = CommonStrings.settings_rageshake_detection_threshold_summary),
                    value = state.sensitivity,
                    enabled = state.isEnabled,
                    // 5 possible values - steps are in ]0, 1[
                    steps = 3,
                    onValueChange = ::onSensitivityChanged
                )
            } else {
                PreferenceText(title = "Rageshaking is not supported by your device")
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun RageshakePreferencesViewPreview(@PreviewParameter(RageshakePreferencesStateProvider::class) state: RageshakePreferencesState) = ElementPreview {
    RageshakePreferencesView(state)
}
