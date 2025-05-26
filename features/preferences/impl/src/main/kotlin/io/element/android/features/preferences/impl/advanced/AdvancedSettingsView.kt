/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.advanced

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import im.vector.app.features.analytics.plan.Interaction
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.architecture.coverage.ExcludeFromCoverage
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.components.preferences.PreferenceDropdown
import io.element.android.libraries.designsystem.components.preferences.PreferencePage
import io.element.android.libraries.designsystem.components.preferences.PreferenceSwitch
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.preview.PreviewWithLargeHeight
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListSectionHeader
import io.element.android.libraries.designsystem.theme.components.ListSupportingText
import io.element.android.libraries.designsystem.theme.components.ListSupportingTextDefaults
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.media.MediaPreviewValue
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.services.analytics.compose.LocalAnalyticsService
import io.element.android.services.analyticsproviders.api.trackers.captureInteraction
import kotlinx.collections.immutable.toPersistentList

@Composable
fun AdvancedSettingsView(
    state: AdvancedSettingsState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val analyticsService = LocalAnalyticsService.current
    PreferencePage(
        modifier = modifier,
        onBackClick = onBackClick,
        title = stringResource(id = CommonStrings.common_advanced_settings)
    ) {
        PreferenceDropdown(
            title = stringResource(id = CommonStrings.common_appearance),
            selectedOption = state.theme,
            options = ThemeOption.entries.toPersistentList(),
            onSelectOption = { themeOption ->
                state.eventSink(AdvancedSettingsEvents.SetTheme(themeOption))
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
                Text(text = stringResource(id = R.string.screen_advanced_settings_media_compression_title))
            },
            supportingContent = {
                Text(text = stringResource(id = R.string.screen_advanced_settings_media_compression_description))
            },
            trailingContent = ListItemContent.Switch(
                checked = state.doesCompressMedia,
            ),
            onClick = {
                val newValue = !state.doesCompressMedia
                analyticsService.captureInteraction(
                    if (newValue) {
                        Interaction.Name.MobileSettingsOptimizeMediaUploadsEnabled
                    } else {
                        Interaction.Name.MobileSettingsOptimizeMediaUploadsDisabled
                    }
                )
                state.eventSink(AdvancedSettingsEvents.SetCompressMedia(newValue))
            }
        )
        ModerationAndSafety(state)
    }
}

@Composable
private fun ModerationAndSafety(
    state: AdvancedSettingsState,
    modifier: Modifier = Modifier,
) {
    PreferenceCategory(
        modifier = modifier,
        title = stringResource(R.string.screen_advanced_settings_moderation_and_safety_section_title),
        showTopDivider = true
    ) {
        PreferenceSwitch(
            title = stringResource(R.string.screen_advanced_settings_hide_invite_avatars_toggle_title),
            isChecked = state.hideInviteAvatars,
            onCheckedChange = {
                state.eventSink(AdvancedSettingsEvents.SetHideInviteAvatars(it))
            },
        )
        ListSectionHeader(
            title = stringResource(R.string.screen_advanced_settings_show_media_timeline_title),
            hasDivider = false,
            description = {
                ListSupportingText(
                    text = stringResource(R.string.screen_advanced_settings_show_media_timeline_subtitle),
                    contentPadding = ListSupportingTextDefaults.Padding.None,
                )
            }
        )
        ListItem(
            headlineContent = { Text(text = stringResource(R.string.screen_advanced_settings_show_media_timeline_always_hide)) },
            leadingContent = ListItemContent.RadioButton(selected = state.timelineMediaPreviewValue == MediaPreviewValue.Off, compact = true),
            onClick = {
                state.eventSink(AdvancedSettingsEvents.SetTimelineMediaPreviewValue(MediaPreviewValue.Off))
            },
        )
        ListItem(
            headlineContent = { Text(text = stringResource(R.string.screen_advanced_settings_show_media_timeline_private_rooms)) },
            leadingContent = ListItemContent.RadioButton(selected = state.timelineMediaPreviewValue == MediaPreviewValue.Private, compact = true),
            onClick = {
                state.eventSink(AdvancedSettingsEvents.SetTimelineMediaPreviewValue(MediaPreviewValue.Private))
            },
        )
        ListItem(
            headlineContent = { Text(text = stringResource(R.string.screen_advanced_settings_show_media_timeline_always_show)) },
            leadingContent = ListItemContent.RadioButton(selected = state.timelineMediaPreviewValue == MediaPreviewValue.On, compact = true),
            onClick = {
                state.eventSink(AdvancedSettingsEvents.SetTimelineMediaPreviewValue(MediaPreviewValue.On))
            },
        )
    }
}

@PreviewWithLargeHeight
@Composable
internal fun AdvancedSettingsViewLightPreview(@PreviewParameter(AdvancedSettingsStateProvider::class) state: AdvancedSettingsState) =
    ElementPreviewLight { ContentToPreview(state) }

@PreviewWithLargeHeight
@Composable
internal fun AdvancedSettingsViewDarkPreview(@PreviewParameter(AdvancedSettingsStateProvider::class) state: AdvancedSettingsState) =
    ElementPreviewDark { ContentToPreview(state) }

@ExcludeFromCoverage
@Composable
private fun ContentToPreview(state: AdvancedSettingsState) {
    AdvancedSettingsView(
        state = state,
        onBackClick = { }
    )
}
