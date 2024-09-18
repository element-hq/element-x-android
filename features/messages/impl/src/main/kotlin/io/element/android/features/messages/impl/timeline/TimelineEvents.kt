/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.event.MessageShield
import kotlin.time.Duration

sealed interface TimelineEvents {
    data class OnScrollFinished(val firstIndex: Int) : TimelineEvents
    data class FocusOnEvent(val eventId: EventId, val debounce: Duration = Duration.ZERO) : TimelineEvents
    data object ClearFocusRequestState : TimelineEvents
    data object OnFocusEventRender : TimelineEvents
    data object JumpToLive : TimelineEvents

    data object HideShieldDialog : TimelineEvents

    /**
     * Events coming from a timeline item.
     */
    sealed interface EventFromTimelineItem : TimelineEvents

    data class ComputeVerifiedUserSendFailure(val event: TimelineItem.Event) : EventFromTimelineItem
    data class ShowShieldDialog(val messageShield: MessageShield) : EventFromTimelineItem
    data class LoadMore(val direction: Timeline.PaginationDirection) : EventFromTimelineItem

    /**
     * Events coming from a poll item.
     */
    sealed interface TimelineItemPollEvents : EventFromTimelineItem

    data class SelectPollAnswer(
        val pollStartId: EventId,
        val answerId: String
    ) : TimelineItemPollEvents

    data class EndPoll(
        val pollStartId: EventId,
    ) : TimelineItemPollEvents

    data class EditPoll(
        val pollStartId: EventId,
    ) : TimelineItemPollEvents
}
