/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.moderation

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.media.MediaPreviewValue

open class ModerationAndSafetyStatePreviewParam : PreviewParameterProvider<ModerationAndSafetyState> {
    override val values: Sequence<ModerationAndSafetyState>
        get() = sequenceOf(
            aModerationAndSafetyState(),
            aModerationAndSafetyState(isSharePresenceEnabled = true),
            aModerationAndSafetyState(hideInviteAvatars = true),
            aModerationAndSafetyState(timelineMediaPreviewValue = MediaPreviewValue.Off),
            aModerationAndSafetyState(setHideInviteAvatarsAction = AsyncAction.Loading),
            aModerationAndSafetyState(setTimelineMediaPreviewAction = AsyncAction.Loading),
            aModerationAndSafetyState(numberOfBlockedUsers = 3),
        )
}

fun aModerationAndSafetyState(
    isSharePresenceEnabled: Boolean = false,
    hideInviteAvatars: Boolean = false,
    timelineMediaPreviewValue: MediaPreviewValue = MediaPreviewValue.On,
    setTimelineMediaPreviewAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    setHideInviteAvatarsAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    numberOfBlockedUsers: Int = 0,
    eventSink: (ModerationAndSafetyEvent) -> Unit = {},
) = ModerationAndSafetyState(
    isSharePresenceEnabled = isSharePresenceEnabled,
    mediaPreviewConfigState = MediaPreviewConfigState(
        hideInviteAvatars = hideInviteAvatars,
        timelineMediaPreviewValue = timelineMediaPreviewValue,
        setTimelineMediaPreviewAction = setTimelineMediaPreviewAction,
        setHideInviteAvatarsAction = setHideInviteAvatarsAction
    ),
    numberOfBlockedUsers = numberOfBlockedUsers,
    eventSink = eventSink
)
