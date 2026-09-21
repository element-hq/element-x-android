/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.moderation

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.architecture.coverage.ExcludeFromCoverage
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.components.preferences.PreferencePage
import io.element.android.libraries.designsystem.components.preferences.PreferenceSwitch
import io.element.android.libraries.designsystem.preview.ElementPreviewBlack
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.preview.PreviewWithLargeHeight
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListSectionHeader
import io.element.android.libraries.designsystem.theme.components.ListSupportingText
import io.element.android.libraries.designsystem.theme.components.ListSupportingTextDefaults
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.snackbar.LocalSnackbarDispatcher
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarHost
import io.element.android.libraries.designsystem.utils.snackbar.collectSnackbarMessageAsState
import io.element.android.libraries.designsystem.utils.snackbar.rememberSnackbarHostState
import io.element.android.libraries.matrix.api.media.MediaPreviewValue
import io.element.android.libraries.ui.strings.CommonStrings

/**
 * Ref: https://www.figma.com/design/kEAcfun9iSpszeUDvdKZ6b/ER-351--Multi-account-in-EX?node-id=606-56426
 */
@Composable
fun ModerationAndSafetyView(
    state: ModerationAndSafetyState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarDispatcher = LocalSnackbarDispatcher.current
    val snackbarMessage by snackbarDispatcher.collectSnackbarMessageAsState()
    val snackbarHostState = rememberSnackbarHostState(snackbarMessage = snackbarMessage)

    PreferencePage(
        modifier = modifier,
        onBackClick = onBackClick,
        title = stringResource(id = CommonStrings.common_moderation_and_safety),
        snackbarHost = {
            SnackbarHost(
                snackbarHostState,
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) {
        PresenceSection(state)
        InvitesSection(state)
        MediaSection(state)
    }
}

@Composable
private fun PresenceSection(state: ModerationAndSafetyState) {
    PreferenceCategory(
        title = stringResource(R.string.screen_moderation_and_safety_share_presence_heading),
    ) {
        ListItem(
            content = {
                Text(text = stringResource(id = R.string.screen_advanced_settings_share_presence))
            },
            supportingContent = {
                Text(text = stringResource(id = R.string.screen_advanced_settings_share_presence_description))
            },
            trailingContent = ListItemContent.Switch(
                checked = state.isSharePresenceEnabled,
            ),
            onClick = { state.eventSink(ModerationAndSafetyEvent.SetSharePresenceEnabled(!state.isSharePresenceEnabled)) }
        )
    }
}

@Composable
private fun InvitesSection(
    state: ModerationAndSafetyState,
    modifier: Modifier = Modifier,
) {
    PreferenceCategory(
        modifier = modifier,
        title = stringResource(R.string.screen_moderation_and_safety_invites_heading),
    ) {
        PreferenceSwitch(
            title = stringResource(R.string.screen_advanced_settings_hide_invite_avatars_toggle_title),
            isChecked = state.mediaPreviewConfigState.hideInviteAvatars,
            onCheckedChange = {
                state.eventSink(ModerationAndSafetyEvent.SetHideInviteAvatars(it))
            },
            enabled = !state.mediaPreviewConfigState.setHideInviteAvatarsAction.isLoading()
        )
    }
}

@Composable
private fun ColumnScope.MediaSection(
    state: ModerationAndSafetyState,
) {
    ListSectionHeader(
        title = stringResource(R.string.screen_advanced_settings_show_media_timeline_title),
        hasDivider = true,
        description = {
            ListSupportingText(
                text = stringResource(R.string.screen_advanced_settings_show_media_timeline_subtitle),
                contentPadding = ListSupportingTextDefaults.Padding.None,
            )
        }
    )
    ListItem(
        content = { Text(text = stringResource(R.string.screen_advanced_settings_show_media_timeline_always_show)) },
        trailingContent = ListItemContent.RadioButton(
            selected = state.mediaPreviewConfigState.timelineMediaPreviewValue == MediaPreviewValue.On,
            compact = true
        ),
        onClick = {
            state.eventSink(ModerationAndSafetyEvent.SetTimelineMediaPreviewValue(MediaPreviewValue.On))
        },
        enabled = !state.mediaPreviewConfigState.setTimelineMediaPreviewAction.isLoading()
    )
    ListItem(
        content = { Text(text = stringResource(R.string.screen_advanced_settings_show_media_timeline_private_rooms)) },
        trailingContent = ListItemContent.RadioButton(
            selected = state.mediaPreviewConfigState.timelineMediaPreviewValue == MediaPreviewValue.Private,
            compact = true
        ),
        onClick = {
            state.eventSink(ModerationAndSafetyEvent.SetTimelineMediaPreviewValue(MediaPreviewValue.Private))
        },
        enabled = !state.mediaPreviewConfigState.setTimelineMediaPreviewAction.isLoading()
    )
    ListItem(
        content = { Text(text = stringResource(R.string.screen_advanced_settings_show_media_timeline_always_hide)) },
        trailingContent = ListItemContent.RadioButton(
            selected = state.mediaPreviewConfigState.timelineMediaPreviewValue == MediaPreviewValue.Off,
            compact = true
        ),
        onClick = {
            state.eventSink(ModerationAndSafetyEvent.SetTimelineMediaPreviewValue(MediaPreviewValue.Off))
        },
        enabled = !state.mediaPreviewConfigState.setTimelineMediaPreviewAction.isLoading()
    )
}

@PreviewWithLargeHeight
@Composable
internal fun ModerationAndSafetyViewLightPreview(@PreviewParameter(ModerationAndSafetyStatePreviewParam::class) state: ModerationAndSafetyState) =
    ElementPreviewLight { ContentToPreview(state) }

@PreviewWithLargeHeight
@Composable
internal fun ModerationAndSafetyViewDarkPreview(@PreviewParameter(ModerationAndSafetyStatePreviewParam::class) state: ModerationAndSafetyState) =
    ElementPreviewDark { ContentToPreview(state) }

@PreviewWithLargeHeight
@Composable
internal fun ModerationAndSafetyViewBlackPreview(@PreviewParameter(ModerationAndSafetyStatePreviewParam::class) state: ModerationAndSafetyState) =
    ElementPreviewBlack { ContentToPreview(state) }

@ExcludeFromCoverage
@Composable
private fun ContentToPreview(state: ModerationAndSafetyState) {
    ModerationAndSafetyView(
        state = state,
        onBackClick = { }
    )
}
