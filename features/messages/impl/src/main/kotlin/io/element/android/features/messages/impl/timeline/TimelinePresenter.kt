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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.element.android.features.messages.impl.timeline.factories.TimelineItemsFactory
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MessageEventType
import io.element.android.libraries.matrix.api.timeline.item.event.TimelineItemEventOrigin
import io.element.android.libraries.matrix.ui.room.canSendEventAsState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val backPaginationEventLimit = 20
private const val backPaginationPageSize = 50

class TimelinePresenter @Inject constructor(
    private val timelineItemsFactory: TimelineItemsFactory,
    private val room: MatrixRoom,
    private val dispatchers: CoroutineDispatchers,
    private val appScope: CoroutineScope,
) : Presenter<TimelineState> {

    private val timeline = room.timeline

    @Composable
    override fun present(): TimelineState {
        val localScope = rememberCoroutineScope()
        val highlightedEventId: MutableState<EventId?> = rememberSaveable {
            mutableStateOf(null)
        }

        var lastReadReceiptIndex by rememberSaveable { mutableStateOf(Int.MAX_VALUE) }
        var lastReadReceiptId by rememberSaveable { mutableStateOf<EventId?>(null) }

        val timelineItems by timelineItemsFactory.collectItemsAsState()
        val paginationState by timeline.paginationState.collectAsState()
        val syncUpdateFlow = room.syncUpdateFlow.collectAsState()
        val userHasPermissionToSendMessage by room.canSendEventAsState(type = MessageEventType.ROOM_MESSAGE, updateKey = syncUpdateFlow.value)

        val prevMostRecentItemId = rememberSaveable { mutableStateOf<String?>(null) }
        val hasNewItems = remember { mutableStateOf(false) }

        fun CoroutineScope.sendReadReceiptIfNeeded(firstVisibleIndex: Int) = launch(dispatchers.computation) {
            // Get last valid EventId seen by the user, as the first index might refer to a Virtual item
            val eventId = getLastEventIdBeforeOrAt(firstVisibleIndex, timelineItems)
            if (eventId != null && firstVisibleIndex <= lastReadReceiptIndex && eventId != lastReadReceiptId) {
                lastReadReceiptIndex = firstVisibleIndex
                lastReadReceiptId = eventId
                timeline.sendReadReceipt(eventId)
            }
        }

        fun handleEvents(event: TimelineEvents) {
            when (event) {
                TimelineEvents.LoadMore -> localScope.paginateBackwards()
                is TimelineEvents.SetHighlightedEvent -> highlightedEventId.value = event.eventId
                is TimelineEvents.OnScrollFinished -> {
                    if (event.firstIndex == 0) {
                        hasNewItems.value = false
                    }
                    appScope.sendReadReceiptIfNeeded(event.firstIndex)
                }
            }
        }

        LaunchedEffect(timelineItems.size) {
            computeHasNewItems(timelineItems, prevMostRecentItemId, hasNewItems)
        }

        LaunchedEffect(Unit) {
            timeline
                .timelineItems
                .onEach(timelineItemsFactory::replaceWith)
                .onEach { timelineItems ->
                    if (timelineItems.isEmpty()) {
                        paginateBackwards()
                    }
                }
                .launchIn(this)
        }

        return TimelineState(
            highlightedEventId = highlightedEventId.value,
            canReply = userHasPermissionToSendMessage,
            paginationState = paginationState,
            timelineItems = timelineItems,
            hasNewItems = hasNewItems.value,
            eventSink = ::handleEvents
        )
    }

    private suspend fun computeHasNewItems(
        timelineItems: ImmutableList<TimelineItem>,
        prevMostRecentItemId: MutableState<String?>,
        hasNewItemsState: MutableState<Boolean>
    ) = withContext(dispatchers.computation) {
        val newMostRecentItem = timelineItems.firstOrNull()
        val prevMostRecentItemIdValue = prevMostRecentItemId.value
        val newMostRecentItemId = newMostRecentItem?.identifier()
        hasNewItemsState.value = prevMostRecentItemIdValue != null &&
            newMostRecentItem is TimelineItem.Event &&
            newMostRecentItem.origin != TimelineItemEventOrigin.PAGINATION &&
            newMostRecentItemId != prevMostRecentItemIdValue
        prevMostRecentItemId.value = newMostRecentItemId
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
}
