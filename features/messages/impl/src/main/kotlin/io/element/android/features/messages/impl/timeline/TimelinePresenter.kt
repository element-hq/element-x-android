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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.messages.impl.MessagesNavigator
import io.element.android.features.messages.impl.timeline.factories.TimelineItemsFactory
import io.element.android.features.messages.impl.timeline.model.NewEventState
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.voicemessages.timeline.RedactedVoiceMessageManager
import io.element.android.features.poll.api.actions.EndPollAction
import io.element.android.features.poll.api.actions.SendPollResponseAction
import io.element.android.features.preferences.api.store.SessionPreferencesStore
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MessageEventType
import io.element.android.libraries.matrix.api.room.roomMembers
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import io.element.android.libraries.matrix.api.timeline.item.event.TimelineItemEventOrigin
import io.element.android.libraries.matrix.ui.room.canSendMessageAsState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TimelinePresenter @AssistedInject constructor(
    private val timelineItemsFactory: TimelineItemsFactory,
    private val timelineItemIndexer: TimelineItemIndexer,
    private val room: MatrixRoom,
    private val dispatchers: CoroutineDispatchers,
    private val appScope: CoroutineScope,
    @Assisted private val navigator: MessagesNavigator,
    private val redactedVoiceMessageManager: RedactedVoiceMessageManager,
    private val sendPollResponseAction: SendPollResponseAction,
    private val endPollAction: EndPollAction,
    private val sessionPreferencesStore: SessionPreferencesStore,
    private val timelineController: TimelineController,
) : Presenter<TimelineState> {
    @AssistedFactory
    interface Factory {
        fun create(navigator: MessagesNavigator): TimelinePresenter
    }

    @Composable
    override fun present(): TimelineState {
        val localScope = rememberCoroutineScope()
        val focusedEventId: MutableState<EventId?> = rememberSaveable {
            mutableStateOf(null)
        }
        val focusRequestState: MutableState<FocusRequestState> = remember {
            mutableStateOf(FocusRequestState.None)
        }

        val lastReadReceiptId = rememberSaveable { mutableStateOf<EventId?>(null) }

        val timelineItems by timelineItemsFactory.collectItemsAsState()
        val roomInfo by room.roomInfoFlow.collectAsState(initial = null)

        val syncUpdateFlow = room.syncUpdateFlow.collectAsState()

        val userHasPermissionToSendMessage by room.canSendMessageAsState(type = MessageEventType.ROOM_MESSAGE, updateKey = syncUpdateFlow.value)
        val userHasPermissionToSendReaction by room.canSendMessageAsState(type = MessageEventType.REACTION, updateKey = syncUpdateFlow.value)

        val prevMostRecentItemId = rememberSaveable { mutableStateOf<String?>(null) }

        val newEventState = remember { mutableStateOf(NewEventState.None) }

        val isSendPublicReadReceiptsEnabled by sessionPreferencesStore.isSendPublicReadReceiptsEnabled().collectAsState(initial = true)
        val renderReadReceipts by sessionPreferencesStore.isRenderReadReceiptsEnabled().collectAsState(initial = true)
        val isLive by timelineController.isLive().collectAsState(initial = true)

        fun handleEvents(event: TimelineEvents) {
            when (event) {
                is TimelineEvents.LoadMore -> {
                    localScope.launch {
                        timelineController.paginate(direction = event.direction)
                    }
                }
                is TimelineEvents.OnScrollFinished -> {
                    if (isLive) {
                        if (event.firstIndex == 0) {
                            newEventState.value = NewEventState.None
                        }
                        println("## sendReadReceiptIfNeeded firstVisibleIndex: ${event.firstIndex}")
                        appScope.sendReadReceiptIfNeeded(
                            firstVisibleIndex = event.firstIndex,
                            timelineItems = timelineItems,
                            lastReadReceiptId = lastReadReceiptId,
                            readReceiptType = if (isSendPublicReadReceiptsEnabled) ReceiptType.READ else ReceiptType.READ_PRIVATE,
                        )
                    } else {
                        newEventState.value = NewEventState.None
                    }
                }
                is TimelineEvents.SelectPollAnswer -> appScope.launch {
                    sendPollResponseAction.execute(
                        pollStartId = event.pollStartId,
                        answerId = event.answerId
                    )
                }
                is TimelineEvents.EndPoll -> appScope.launch {
                    endPollAction.execute(
                        pollStartId = event.pollStartId,
                    )
                }
                is TimelineEvents.EditPoll -> {
                    navigator.onEditPollClick(event.pollStartId)
                }
                is TimelineEvents.FocusOnEvent -> localScope.launch {
                    focusedEventId.value = event.eventId
                    if (timelineItemIndexer.isKnown(event.eventId)) {
                        val index = timelineItemIndexer.indexOf(event.eventId)
                        focusRequestState.value = FocusRequestState.Cached(index)
                    } else {
                        focusRequestState.value = FocusRequestState.Fetching
                        timelineController.focusOnEvent(event.eventId)
                            .fold(
                                onSuccess = {
                                    focusRequestState.value = FocusRequestState.Fetched
                                },
                                onFailure = {
                                    focusRequestState.value = FocusRequestState.Failure(it)
                                }
                            )
                    }
                }
                is TimelineEvents.ClearFocusRequestState -> {
                    focusRequestState.value = FocusRequestState.None
                }
                is TimelineEvents.JumpToLive -> {
                    timelineController.focusOnLive()
                }
            }
        }

        LaunchedEffect(timelineItems.size) {
            computeNewItemState(timelineItems, prevMostRecentItemId, newEventState)
        }

        LaunchedEffect(timelineItems.size, focusRequestState.value, focusedEventId.value) {
            val currentFocusedEventId = focusedEventId.value
            if (focusRequestState.value is FocusRequestState.Fetched && currentFocusedEventId != null) {
                if (timelineItemIndexer.isKnown(currentFocusedEventId)) {
                    val index = timelineItemIndexer.indexOf(currentFocusedEventId)
                    focusRequestState.value = FocusRequestState.Cached(index)
                }
            }
        }

        LaunchedEffect(Unit) {
            combine(timelineController.timelineItems(), room.membersStateFlow) { items, membersState ->
                timelineItemsFactory.replaceWith(
                    timelineItems = items,
                    roomMembers = membersState.roomMembers().orEmpty()
                )
                items
            }
                .onEach(redactedVoiceMessageManager::onEachMatrixTimelineItem)
                .launchIn(this)
        }

        val timelineRoomInfo by remember {
            derivedStateOf {
                TimelineRoomInfo(
                    name = room.displayName,
                    isDm = room.isDm,
                    userHasPermissionToSendMessage = userHasPermissionToSendMessage,
                    userHasPermissionToSendReaction = userHasPermissionToSendReaction,
                    isCallOngoing = roomInfo?.hasRoomCall.orFalse(),
                )
            }
        }
        return TimelineState(
            timelineRoomInfo = timelineRoomInfo,
            timelineItems = timelineItems,
            renderReadReceipts = renderReadReceipts,
            newEventState = newEventState.value,
            isLive = isLive,
            focusedEventId = focusedEventId.value,
            focusRequestState = focusRequestState.value,
            eventSink = { handleEvents(it) }
        )
    }

    /**
     * This method compute the hasNewItem state passed as a [MutableState] each time the timeline items size changes.
     * Basically, if we got new timeline event from sync or local, either from us or another user, we update the state so we tell we have new items.
     * The state never goes back to None from this method, but need to be reset from somewhere else.
     */
    private suspend fun computeNewItemState(
        timelineItems: ImmutableList<TimelineItem>,
        prevMostRecentItemId: MutableState<String?>,
        newEventState: MutableState<NewEventState>
    ) = withContext(dispatchers.computation) {
        // FromMe is prioritized over FromOther, so skip if we already have a FromMe
        if (newEventState.value == NewEventState.FromMe) {
            return@withContext
        }
        val newMostRecentItem = timelineItems.firstOrNull()
        val prevMostRecentItemIdValue = prevMostRecentItemId.value
        val newMostRecentItemId = newMostRecentItem?.identifier()
        val hasNewEvent = prevMostRecentItemIdValue != null &&
            newMostRecentItem is TimelineItem.Event &&
            newMostRecentItem.origin != TimelineItemEventOrigin.PAGINATION &&
            newMostRecentItemId != prevMostRecentItemIdValue

        if (hasNewEvent) {
            val newMostRecentEvent = newMostRecentItem as? TimelineItem.Event
            // Scroll to bottom if the new event is from me, even if sent from another device
            val fromMe = newMostRecentEvent?.isMine == true
            newEventState.value = if (fromMe) {
                NewEventState.FromMe
            } else {
                NewEventState.FromOther
            }
        }
        prevMostRecentItemId.value = newMostRecentItemId
    }

    private fun CoroutineScope.sendReadReceiptIfNeeded(
        firstVisibleIndex: Int,
        timelineItems: ImmutableList<TimelineItem>,
        lastReadReceiptId: MutableState<EventId?>,
        readReceiptType: ReceiptType,
    ) = launch(dispatchers.computation) {
        // If we are at the bottom of timeline, we mark the room as read.
        if (firstVisibleIndex == 0) {
            room.markAsRead(receiptType = readReceiptType)
        } else {
            // Get last valid EventId seen by the user, as the first index might refer to a Virtual item
            val eventId = getLastEventIdBeforeOrAt(firstVisibleIndex, timelineItems)
            if (eventId != null && eventId != lastReadReceiptId.value) {
                lastReadReceiptId.value = eventId
                room.liveTimeline.sendReadReceipt(eventId = eventId, receiptType = readReceiptType)
            }
        }
    }

    private fun getLastEventIdBeforeOrAt(index: Int, items: ImmutableList<TimelineItem>): EventId? {
        for (i in index until items.count()) {
            val item = items[i]
            if (item is TimelineItem.Event) {
                return item.eventId
            }
        }
        return null
    }
}
