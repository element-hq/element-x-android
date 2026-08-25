/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import io.element.android.features.messages.impl.timeline.components.MessageShieldData
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.ui.media.contentvalidation.ContentValidationState
import kotlin.time.Duration

sealed interface TimelineEvent {
    data class OnScrollFinished(val firstIndex: Int) : TimelineEvent

    /**
     * @param eventId the event to scroll to and highlight.
     * @param debounce how long to wait before starting, so a burst of requests only runs the last one.
     * @param fromReply true when the jump was started by tapping a reply, which is the only case the timeline
     * remembers so that it can offer to come back.
     */
    data class FocusOnEvent(val eventId: EventId, val debounce: Duration = Duration.ZERO, val fromReply: Boolean = false) : TimelineEvent

    data object JumpBack : TimelineEvent
    data object ClearFocusRequestState : TimelineEvent
    data object OnFocusEventRender : TimelineEvent
    data object JumpToLive : TimelineEvent

    data object HideShieldDialog : TimelineEvent

    data object MarkAllAsRead : TimelineEvent

    /**
     * Events coming from a timeline item.
     */
    sealed interface TimelineItemEvent : TimelineEvent

    data class ShowSendFailureDialog(val event: TimelineItem.Event) : TimelineItemEvent
    data object HideSendFailureDialog : TimelineEvent

    data class ComputeVerifiedUserSendFailure(val event: TimelineItem.Event) : TimelineItemEvent
    data class ShowShieldDialog(val messageShieldData: MessageShieldData) : TimelineItemEvent
    data class LoadMore(val direction: Timeline.PaginationDirection) : TimelineItemEvent
    data class OpenThread(val threadRootEventId: ThreadId, val focusedEvent: EventId?) : TimelineItemEvent

    /**
     * Navigate to the predecessor or successor room of the current room.
     */
    data class NavigateToPredecessorOrSuccessorRoom(val roomId: RoomId) : TimelineItemEvent

    /**
     * Events coming from a poll item.
     */
    sealed interface TimelineItemPollEvent : TimelineItemEvent

    data class SelectPollAnswer(
        val pollStartId: EventId,
        val answerId: String
    ) : TimelineItemPollEvent

    data class EndPoll(
        val pollStartId: EventId,
    ) : TimelineItemPollEvent

    data class EditPoll(
        val pollStartId: EventId,
    ) : TimelineItemPollEvent

    data object StopLiveLocationShare : TimelineItemEvent

    data class ValidateMedia(
        val eventId: EventId,
        val mediaSources: List<MediaSource>,
        val validationState: ContentValidationState
    ) : TimelineItemEvent
}
