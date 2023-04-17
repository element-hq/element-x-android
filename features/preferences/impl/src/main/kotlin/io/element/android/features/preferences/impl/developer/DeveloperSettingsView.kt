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

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.features.preferences.impl.developer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.designsystem.components.preferences.PreferenceTopAppBar
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.featureflag.ui.FeatureListView
import io.element.android.libraries.featureflag.ui.model.FeatureUiModel
import io.element.android.libraries.ui.strings.R

@Composable
fun DeveloperSettingsView(
    state: DeveloperSettingsState,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit,
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding(),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            PreferenceTopAppBar(
                title = stringResource(id = R.string.common_developer_options),
                onBackPressed = onBackPressed,
            )
        },
        content = {
            FeatureListContent(it, state)
        }
    )
}

@Composable
fun FeatureListContent(
    paddingValues: PaddingValues,
    state: DeveloperSettingsState,
    modifier: Modifier = Modifier
) {

    fun onFeatureEnabled(feature: FeatureUiModel, isEnabled: Boolean) {
        state.eventSink(DeveloperSettingsEvents.UpdateEnabledFeature(feature, isEnabled))
    }

    Box(
        modifier = modifier
            .padding(paddingValues)
            .fillMaxSize()
    ) {
        FeatureListView(features = state.features, onCheckedChange = ::onFeatureEnabled)
    }
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
        onBackPressed = {}
    )
}
