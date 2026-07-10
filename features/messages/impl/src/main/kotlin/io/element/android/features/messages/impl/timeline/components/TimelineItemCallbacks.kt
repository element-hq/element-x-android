/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.wysiwyg.link.Link

/**
 * Callbacks for the user interactions on a timeline item.
 */
@Immutable
data class TimelineItemCallbacks(
    val onUserDataClick: (MatrixUser) -> Unit = {},
    val onLinkClick: (Link) -> Unit = {},
    val onLinkLongClick: (Link) -> Unit = {},
    val onContentClick: (TimelineItem.Event) -> Unit = {},
    val onGalleryItemClick: (event: TimelineItem.Event, index: Int) -> Unit = { _, _ -> },
    val onLongClick: (TimelineItem.Event) -> Unit = {},
    val inReplyToClick: (EventId) -> Unit = {},
    val onReactionClick: (key: String, TimelineItem.Event) -> Unit = { _, _ -> },
    val onReactionLongClick: (key: String, TimelineItem.Event) -> Unit = { _, _ -> },
    val onMoreReactionsClick: (TimelineItem.Event) -> Unit = {},
    val onReadReceiptClick: (TimelineItem.Event) -> Unit = {},
    val onSwipeToReply: (TimelineItem.Event) -> Unit = {},
    val onStopSharingLiveLocation: () -> Unit = {},
    val onSelectPollAnswer: (pollStartId: EventId, answerId: String) -> Unit = { _, _ -> },
    val onEndPoll: (pollStartId: EventId) -> Unit = {},
    val onEditPoll: (pollStartId: EventId) -> Unit = {},
    val onShowMessageShieldInfo: (MessageShieldData?) -> Unit = {},
    val onSendFailureWithUnverifiedSessions: (TimelineItem.Event) -> Unit = {},
)

data class TimelineEventContentCallbacks(
    val onLinkClick: (Link) -> Unit = {},
    val onLinkLongClick: (Link) -> Unit = {},
    val onContentClick: (() -> Unit)? = {},
    val onGalleryItemClick: (index: Int) -> Unit = {},
    val onLongClick: () -> Unit = {},
    val onStopSharingLiveLocation: () -> Unit = {},
    val onSelectPollAnswer: (pollStartId: EventId, answerId: String) -> Unit = { _, _ -> },
    val onEndPoll: (pollStartId: EventId) -> Unit = {},
    val onEditPoll: (pollStartId: EventId) -> Unit = {},
)

@Composable
fun TimelineItemCallbacks.toTimelineEventContentCallbacks(
    event: TimelineItem.Event,
): TimelineEventContentCallbacks {
    return remember(event) {
        TimelineEventContentCallbacks(
            onLinkClick = this.onLinkClick,
            onLinkLongClick = this.onLinkLongClick,
            onContentClick = { this.onContentClick(event) },
            onGalleryItemClick = { index -> this.onGalleryItemClick(event, index) },
            onLongClick = { this.onLongClick(event) },
            onStopSharingLiveLocation = this.onStopSharingLiveLocation,
            onSelectPollAnswer = this.onSelectPollAnswer,
            onEndPoll = this.onEndPoll,
            onEditPoll = this.onEditPoll,
        )
    }
}
