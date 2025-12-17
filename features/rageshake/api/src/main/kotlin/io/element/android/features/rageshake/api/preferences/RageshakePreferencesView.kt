/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Text
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
        if (state.isFeatureEnabled) {
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
                    ListItem(
                        headlineContent = {
                            Text("Rageshaking is not supported by your device")
                        },
                    )
                }
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun RageshakePreferencesViewPreview(@PreviewParameter(RageshakePreferencesStateProvider::class) state: RageshakePreferencesState) = ElementPreview {
    RageshakePreferencesView(state)
}
