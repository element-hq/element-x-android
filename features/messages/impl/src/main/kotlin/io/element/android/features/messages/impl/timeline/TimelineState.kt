/*
 * Copyright (c) 2022 New Vector Ltd
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

import androidx.compose.runtime.Immutable
import io.element.android.features.messages.impl.timeline.model.NewEventState
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.matrix.api.core.EventId
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class TimelineState(
    val timelineItems: ImmutableList<TimelineItem>,
    val timelineRoomInfo: TimelineRoomInfo,
    val renderReadReceipts: Boolean,
    val newEventState: NewEventState,
    val isLive: Boolean,
    val focusRequestState: FocusRequestState,
    val eventSink: (TimelineEvents) -> Unit,
) {
    val hasAnyEvent = timelineItems.any { it is TimelineItem.Event }
    val focusedEventId = focusRequestState.eventId()
}

@Immutable
sealed interface FocusRequestState {
    data object None : FocusRequestState
    data class Loading(val eventId: EventId) : FocusRequestState
    data class Success(
        val eventId: EventId,
        val index: Int = -1,
        // This is used to know if the event has been rendered yet.
        val rendered: Boolean = false,
    ) : FocusRequestState {
        val isIndexed
            get() = index != -1
    }

    data class Failure(val throwable: Throwable) : FocusRequestState

    fun eventId(): EventId? {
        return when (this) {
            is Loading -> eventId
            is Success -> eventId
            else -> null
        }
    }
}

@Immutable
data class TimelineRoomInfo(
    val isDm: Boolean,
    val name: String?,
    val userHasPermissionToSendMessage: Boolean,
    val userHasPermissionToSendReaction: Boolean,
    val isCallOngoing: Boolean,
)
