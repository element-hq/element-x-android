/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.advanced

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.media.MediaPreviewValue

open class AdvancedSettingsStateProvider : PreviewParameterProvider<AdvancedSettingsState> {
    override val values: Sequence<AdvancedSettingsState>
        get() = sequenceOf(
            aAdvancedSettingsState(),
            aAdvancedSettingsState(isDeveloperModeEnabled = true),
            aAdvancedSettingsState(isSharePresenceEnabled = true),
            aAdvancedSettingsState(doesCompressMedia = true),
            aAdvancedSettingsState(hideInviteAvatars = true),
            aAdvancedSettingsState(timelineMediaPreviewValue = MediaPreviewValue.Off),
            aAdvancedSettingsState(setHideInviteAvatarsAction = AsyncAction.Loading),
            aAdvancedSettingsState(setTimelineMediaPreviewAction = AsyncAction.Loading),
        )
}

fun aAdvancedSettingsState(
    isDeveloperModeEnabled: Boolean = false,
    isSharePresenceEnabled: Boolean = false,
    doesCompressMedia: Boolean = false,
    theme: ThemeOption = ThemeOption.System,
    hideInviteAvatars: Boolean = false,
    timelineMediaPreviewValue: MediaPreviewValue = MediaPreviewValue.On,
    setTimelineMediaPreviewAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    setHideInviteAvatarsAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (AdvancedSettingsEvents) -> Unit = {},
) = AdvancedSettingsState(
    isDeveloperModeEnabled = isDeveloperModeEnabled,
    isSharePresenceEnabled = isSharePresenceEnabled,
    doesCompressMedia = doesCompressMedia,
    theme = theme,
    mediaPreviewConfigState = MediaPreviewConfigState(
        hideInviteAvatars = hideInviteAvatars,
        timelineMediaPreviewValue = timelineMediaPreviewValue,
        setTimelineMediaPreviewAction = setTimelineMediaPreviewAction,
        setHideInviteAvatarsAction = setHideInviteAvatarsAction
    ),
    eventSink = eventSink
)
