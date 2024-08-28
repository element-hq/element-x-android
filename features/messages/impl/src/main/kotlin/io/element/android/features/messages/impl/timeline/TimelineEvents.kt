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

package io.element.android.features.messages.impl.timeline

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

    data class ShowShieldDialog(val messageShield: MessageShield) : TimelineEvents
    data object HideShieldDialog : TimelineEvents

    /**
     * Events coming from a timeline item.
     */
    sealed interface EventFromTimelineItem : TimelineEvents

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
