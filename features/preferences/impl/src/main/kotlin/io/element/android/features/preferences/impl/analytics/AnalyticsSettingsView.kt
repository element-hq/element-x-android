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

package io.element.android.features.preferences.impl.analytics

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.analytics.api.preferences.AnalyticsPreferencesView
import io.element.android.libraries.designsystem.components.preferences.PreferenceView
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun AnalyticsSettingsView(
    state: AnalyticsSettingsState,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PreferenceView(
        modifier = modifier,
        onBackPressed = onBackPressed,
        title = stringResource(id = CommonStrings.common_analytics)
    ) {
        AnalyticsPreferencesView(
            state = state.analyticsState,
        )
    }
}

@Preview
@Composable
internal fun AnalyticsSettingsViewLightPreview(@PreviewParameter(AnalyticsSettingsStateProvider::class) state: AnalyticsSettingsState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
internal fun AnalyticsSettingsViewDarkPreview(@PreviewParameter(AnalyticsSettingsStateProvider::class) state: AnalyticsSettingsState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: AnalyticsSettingsState) {
    AnalyticsSettingsView(
        state = state,
        onBackPressed = {},
    )
}
