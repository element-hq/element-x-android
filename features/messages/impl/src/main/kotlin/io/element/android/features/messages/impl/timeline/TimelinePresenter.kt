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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.element.android.features.messages.impl.timeline.factories.TimelineItemsFactory
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MessageEventType
import io.element.android.libraries.matrix.ui.room.canSendEventAsState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val backPaginationEventLimit = 20
private const val backPaginationPageSize = 50

class TimelinePresenter @Inject constructor(
    private val timelineItemsFactory: TimelineItemsFactory,
    private val room: MatrixRoom,
) : Presenter<TimelineState> {

    private val timeline = room.timeline

    @Composable
    override fun present(): TimelineState {
        val localCoroutineScope = rememberCoroutineScope()
        val highlightedEventId: MutableState<EventId?> = rememberSaveable {
            mutableStateOf(null)
        }

        var lastReadMarkerIndex by rememberSaveable { mutableStateOf(Int.MAX_VALUE) }
        var lastReadMarkerId by rememberSaveable { mutableStateOf<EventId?>(null) }

        val timelineItems by timelineItemsFactory.collectItemsAsState()
        val paginationState by timeline.paginationState.collectAsState()

        val syncUpdateFlow = room.syncUpdateFlow.collectAsState()
        val userHasPermissionToSendMessage by room.canSendEventAsState(type = MessageEventType.ROOM_MESSAGE, updateKey = syncUpdateFlow.value)

        fun handleEvents(event: TimelineEvents) {
            when (event) {
                TimelineEvents.LoadMore -> localCoroutineScope.paginateBackwards()
                is TimelineEvents.SetHighlightedEvent -> highlightedEventId.value = event.eventId
                is TimelineEvents.OnScrollFinished -> {
                    // Get last valid EventId seen by the user, as the first index might refer to a Virtual item
                    val eventId = getLastEventIdBeforeOrAt(event.firstIndex, timelineItems) ?: return
                    if (event.firstIndex <= lastReadMarkerIndex && eventId != lastReadMarkerId) {
                        lastReadMarkerIndex = event.firstIndex
                        lastReadMarkerId = eventId
                        localCoroutineScope.sendReadReceipt(eventId)
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            timeline
                .timelineItems
                .onEach(timelineItemsFactory::replaceWith)
                .launchIn(this)
        }

        return TimelineState(
            highlightedEventId = highlightedEventId.value,
            canReply = userHasPermissionToSendMessage,
            paginationState = paginationState,
            timelineItems = timelineItems,
            eventSink = ::handleEvents
        )
    }

    private fun getLastEventIdBeforeOrAt(index: Int, items: ImmutableList<TimelineItem>): EventId? {
        for (item in items.subList(index, items.count())) {
            if (item is TimelineItem.Event) {
                return item.eventId
            }
        }
        return null
    }

    private fun CoroutineScope.paginateBackwards() = launch {
        timeline.paginateBackwards(backPaginationEventLimit, backPaginationPageSize)
    }

    private fun CoroutineScope.sendReadReceipt(eventId: EventId) = launch {
        timeline.sendReadReceipt(eventId)
    }
}
