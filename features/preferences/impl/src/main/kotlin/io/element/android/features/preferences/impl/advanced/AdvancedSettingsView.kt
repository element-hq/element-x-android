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

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.Theme
import io.element.android.compound.theme.themes
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.components.dialogs.ListOption
import io.element.android.libraries.designsystem.components.dialogs.SingleSelectionDialog
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.components.preferences.PreferencePage
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun AdvancedSettingsView(
    state: AdvancedSettingsState,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PreferencePage(
        modifier = modifier,
        onBackPressed = onBackPressed,
        title = stringResource(id = CommonStrings.common_advanced_settings)
    ) {
        ListItem(
            headlineContent = {
                Text(text = stringResource(id = CommonStrings.common_appearance))
            },
            trailingContent = ListItemContent.Text(
                state.theme.toHumanReadable()
            ),
            onClick = {
                state.eventSink(AdvancedSettingsEvents.ChangeTheme)
            }
        )
        ListItem(
            headlineContent = {
                Text(text = stringResource(id = CommonStrings.action_view_source))
            },
            supportingContent = {
                Text(text = stringResource(id = R.string.screen_advanced_settings_view_source_description))
            },
            trailingContent = ListItemContent.Switch(
                checked = state.isDeveloperModeEnabled,
            ),
            onClick = { state.eventSink(AdvancedSettingsEvents.SetDeveloperModeEnabled(!state.isDeveloperModeEnabled)) }
        )
        ListItem(
            headlineContent = {
                Text(text = stringResource(id = R.string.screen_advanced_settings_share_presence))
            },
            supportingContent = {
                Text(text = stringResource(id = R.string.screen_advanced_settings_share_presence_description))
            },
            trailingContent = ListItemContent.Switch(
                checked = state.isSharePresenceEnabled,
            ),
            onClick = { state.eventSink(AdvancedSettingsEvents.SetSharePresenceEnabled(!state.isSharePresenceEnabled)) }
        )
        ListItem(
            headlineContent = {
                Text(text = stringResource(id = R.string.screen_advanced_settings_push_provider_android))
            },
            trailingContent = when (state.currentPushDistributor) {
                AsyncAction.Uninitialized,
                AsyncAction.Confirming,
                AsyncAction.Loading -> ListItemContent.Custom {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .progressSemantics()
                            .size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
                is AsyncAction.Failure -> ListItemContent.Text(
                    stringResource(id = CommonStrings.common_error)
                )
                is AsyncAction.Success -> ListItemContent.Text(
                    state.currentPushDistributor.dataOrNull() ?: ""
                )
            },
            onClick = {
                if (state.currentPushDistributor.isReady()) {
                    state.eventSink(AdvancedSettingsEvents.ChangePushProvider)
                }
            }
        )
        ListItem(
            headlineContent = {
                Text(text = stringResource(id = R.string.screen_advanced_settings_reaction_search))
            },
            supportingContent = {
                Text(text = stringResource(id = R.string.screen_advanced_settings_reaction_search_description))
            },
            trailingContent = ListItemContent.Switch(
                checked = state.isReactionPickerSearchEnabled,
            ),
            onClick = { state.eventSink(AdvancedSettingsEvents.SetReactionPickerSearchEnabled(!state.isReactionPickerSearchEnabled)) }
        )
    }

    if (state.showChangeThemeDialog) {
        SingleSelectionDialog(
            options = getOptions(),
            initialSelection = themes.indexOf(state.theme),
            onOptionSelected = {
                state.eventSink(
                    AdvancedSettingsEvents.SetTheme(
                        themes[it]
                    )
                )
            },
            onDismissRequest = { state.eventSink(AdvancedSettingsEvents.CancelChangeTheme) },
        )
    }

    if (state.showChangePushProviderDialog) {
        SingleSelectionDialog(
            title = stringResource(id = R.string.screen_advanced_settings_choose_distributor_dialog_title_android),
            options = state.availablePushDistributors.map {
                ListOption(title = it)
            }.toImmutableList(),
            initialSelection = state.availablePushDistributors.indexOf(state.currentPushDistributor.dataOrNull()),
            onOptionSelected = { index ->
                state.eventSink(
                    AdvancedSettingsEvents.SetPushProvider(index)
                )
            },
            onDismissRequest = { state.eventSink(AdvancedSettingsEvents.CancelChangePushProvider) },
        )
    }
}

@Composable
private fun getOptions(): ImmutableList<ListOption> {
    return themes.map {
        ListOption(title = it.toHumanReadable())
    }.toImmutableList()
}

@Composable
private fun Theme.toHumanReadable(): String {
    return stringResource(
        id = when (this) {
            Theme.System -> CommonStrings.common_system
            Theme.Dark -> CommonStrings.common_dark
            Theme.Light -> CommonStrings.common_light
        }
    )
}

@PreviewsDayNight
@Composable
internal fun AdvancedSettingsViewPreview(@PreviewParameter(AdvancedSettingsStateProvider::class) state: AdvancedSettingsState) =
    ElementPreview {
        AdvancedSettingsView(state = state, onBackPressed = { })
    }
