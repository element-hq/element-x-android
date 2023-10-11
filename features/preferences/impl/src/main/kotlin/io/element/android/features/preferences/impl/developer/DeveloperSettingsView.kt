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
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.rageshake.api.preferences.RageshakePreferencesView
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.components.preferences.PreferenceText
import io.element.android.libraries.designsystem.components.preferences.PreferencePage
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.featureflag.ui.FeatureListView
import io.element.android.libraries.featureflag.ui.model.FeatureUiModel
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun DeveloperSettingsView(
    state: DeveloperSettingsState,
    onOpenShowkase: () -> Unit,
    onOpenConfigureTracing: () -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PreferencePage(
        modifier = modifier,
        onBackPressed = onBackPressed,
        title = stringResource(id = CommonStrings.common_developer_options)
    ) {
        // Note: this is OK to hardcode strings in this debug screen.
        PreferenceCategory(title = "Feature flags") {
            FeatureListContent(state)
        }
        PreferenceCategory(title = "Rust SDK") {
            PreferenceText(
                title = "Configure tracing",
                onClick = onOpenConfigureTracing,
            )
        }
        PreferenceCategory(title = "Showkase") {
            PreferenceText(
                title = "Open Showkase browser",
                onClick = onOpenShowkase
            )
        }
        RageshakePreferencesView(
            state = state.rageshakeState,
        )
        PreferenceCategory(title = "Crash", showDivider = false) {
            PreferenceText(
                title = "Crash the app 💥",
                onClick = { error("This crash is a test.") }
            )
        }
        val cache = state.cacheSize
        PreferenceCategory(title = "Cache", showDivider = false) {
            PreferenceText(
                title = "Clear cache",
                currentValue = cache.dataOrNull(),
                loadingCurrentValue = state.cacheSize.isLoading() || state.clearCacheAction.isLoading(),
                onClick = {
                    if (state.clearCacheAction.isLoading().not()) {
                        state.eventSink(DeveloperSettingsEvents.ClearCache)
                    }
                }
            )
        }
    }
}

@Composable
private fun FeatureListContent(
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

@PreviewsDayNight
@Composable
internal fun DeveloperSettingsViewPreview(@PreviewParameter(DeveloperSettingsStateProvider::class) state: DeveloperSettingsState) = ElementPreview {
    DeveloperSettingsView(
        state = state,
        onOpenShowkase = {},
        onOpenConfigureTracing = {},
        onBackPressed = {}
    )
}
