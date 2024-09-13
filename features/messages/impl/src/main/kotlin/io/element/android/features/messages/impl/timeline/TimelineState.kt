/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import androidx.compose.runtime.Immutable
import io.element.android.features.messages.impl.crypto.sendfailure.resolve.ResolveVerifiedUserSendFailureState
import io.element.android.features.messages.impl.timeline.model.NewEventState
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.timeline.item.event.MessageShield
import kotlinx.collections.immutable.ImmutableList
import kotlin.time.Duration

@Immutable
data class TimelineState(
    val timelineItems: ImmutableList<TimelineItem>,
    val timelineRoomInfo: TimelineRoomInfo,
    val renderReadReceipts: Boolean,
    val newEventState: NewEventState,
    val isLive: Boolean,
    val focusRequestState: FocusRequestState,
    // If not null, info will be rendered in a dialog
    val messageShield: MessageShield?,
    val resolveVerifiedUserSendFailureState: ResolveVerifiedUserSendFailureState,
    val eventSink: (TimelineEvents) -> Unit,
) {
    val hasAnyEvent = timelineItems.any { it is TimelineItem.Event }
    val focusedEventId = focusRequestState.eventId()
}

@Immutable
sealed interface FocusRequestState {
    data object None : FocusRequestState
    data class Requested(val eventId: EventId, val debounce: Duration) : FocusRequestState
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
            is Requested -> eventId
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
