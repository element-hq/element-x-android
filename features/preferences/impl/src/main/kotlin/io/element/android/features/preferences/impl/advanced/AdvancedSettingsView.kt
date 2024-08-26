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

import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.sp
import io.element.android.compound.theme.Theme
import io.element.android.compound.theme.themes
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.designsystem.components.dialogs.ListOption
import io.element.android.libraries.designsystem.components.dialogs.SingleSelectionDialog
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.components.preferences.PreferencePage
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.immutableListOf
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
fun AdvancedSettingsView(
    state: AdvancedSettingsState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PreferencePage(
        modifier = modifier,
        onBackClick = onBackClick,
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
                Text(text = stringResource(id = R.string.screen_advanced_settings_skin_tone))
            },
            supportingContent = {
                Text(text = stringResource(id = R.string.screen_advanced_settings_skin_tone_description))
            },
            trailingContent = ListItemContent.Custom {
                Text(
                    text = ("👋" + state.skinTone.orEmpty()),
                    style = LocalTextStyle.current.copy(fontSize = 32.sp),
                )
            },
            onClick = {
                state.eventSink(AdvancedSettingsEvents.ChangeSkinTone)
            }
        )
    }

    if (state.showChangeThemeDialog) {
        SingleSelectionDialog(
            options = getThemeOptions(),
            initialSelection = themes.indexOf(state.theme),
            onSelectOption = {
                state.eventSink(
                    AdvancedSettingsEvents.SetTheme(
                        themes[it]
                    )
                )
            },
            onDismissRequest = { state.eventSink(AdvancedSettingsEvents.CancelChangeTheme) },
        )
    }

    if (state.showChangeSkinToneDialog) {
        SingleSelectionDialog(
            options = getSkinToneOptions(),
            initialSelection = when (state.skinTone) {
                null, "" -> 0
                else -> state.skinTone.codePointAt(0) - 0x1f3fa
            },
            onSelectOption = {
                val tone = when (it) {
                    0 -> null
                    else -> String(intArrayOf(it + 0x1f3fa), 0, 1)
                }
                state.eventSink(AdvancedSettingsEvents.SetSkinTone(tone))
            },
            onDismissRequest = { state.eventSink(AdvancedSettingsEvents.CancelChangeSkinTone) },
        )
    }
}

@Composable
private fun getThemeOptions(): ImmutableList<ListOption> {
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

@Composable
private fun getSkinToneOptions(): ImmutableList<ListOption> {
    val emojis = setOf("👋", "🧑", "🏃")
    val modifiers = sortedMapOf(
        "" to "No modifier",
        "🏻" to "Light Skin Tone",
        "🏼" to "Medium-Light Skin Tone",
        "🏽" to "Medium Skin Tone",
        "🏾" to "Medium-Dark Skin Tone",
        "🏿" to "Dark Skin Tone",
    )

    return modifiers.map { e ->
        ListOption(title = emojis.joinToString(" ") { it + e.key }, subtitle = e.value)
    }.toImmutableList()
}

@PreviewsDayNight
@Composable
internal fun AdvancedSettingsViewPreview(@PreviewParameter(AdvancedSettingsStateProvider::class) state: AdvancedSettingsState) =
    ElementPreview {
        AdvancedSettingsView(state = state, onBackClick = { })
    }
