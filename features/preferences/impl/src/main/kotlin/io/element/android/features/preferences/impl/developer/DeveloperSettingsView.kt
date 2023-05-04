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

package io.element.android.features.preferences.impl.developer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.components.preferences.PreferenceText
import io.element.android.libraries.designsystem.components.preferences.PreferenceView
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.featureflag.ui.FeatureListView
import io.element.android.libraries.featureflag.ui.model.FeatureUiModel
import io.element.android.libraries.ui.strings.R

@Composable
fun DeveloperSettingsView(
    state: DeveloperSettingsState,
    modifier: Modifier = Modifier,
    onOpenShowkase: () -> Unit,
    onBackPressed: () -> Unit,
) {
    PreferenceView(
        modifier = modifier,
        onBackPressed = onBackPressed,
        title = stringResource(id = R.string.common_developer_options)
    ) {
        // Note: this is OK to hardcode strings in this debug screen.
        PreferenceCategory(title = "Feature flags") {
            FeatureListContent(state)
        }
        PreferenceCategory(title = "Showkase") {
            PreferenceText(
                title = "Open Showkase browser",
                onClick = onOpenShowkase
            )
        }
    }
}

@Composable
fun FeatureListContent(
    state: DeveloperSettingsState,
    modifier: Modifier = Modifier
) {
    fun onFeatureEnabled(feature: FeatureUiModel, isEnabled: Boolean) {
        state.eventSink(DeveloperSettingsEvents.UpdateEnabledFeature(feature, isEnabled))
    }

    FeatureListView(
        modifier = modifier,
        features = state.features,
        onCheckedChange = ::onFeatureEnabled,
    )
}

@Preview
@Composable
fun DeveloperSettingsViewLightPreview(@PreviewParameter(DeveloperSettingsStateProvider::class) state: DeveloperSettingsState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
fun DeveloperSettingsViewDarkPreview(@PreviewParameter(DeveloperSettingsStateProvider::class) state: DeveloperSettingsState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: DeveloperSettingsState) {
    DeveloperSettingsView(
        state = state,
        onOpenShowkase = {},
        onBackPressed = {}
    )
}
