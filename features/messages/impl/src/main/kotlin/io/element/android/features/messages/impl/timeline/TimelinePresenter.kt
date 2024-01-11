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
import androidx.compose.runtime.mutableIntStateOf
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
import io.element.android.features.messages.impl.timeline.session.SessionState
import io.element.android.features.messages.impl.voicemessages.timeline.RedactedVoiceMessageManager
import io.element.android.features.poll.api.actions.EndPollAction
import io.element.android.features.poll.api.actions.SendPollResponseAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.encryption.BackupState
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MessageEventType
import io.element.android.libraries.matrix.api.room.roomMembers
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import io.element.android.libraries.matrix.api.timeline.item.event.TimelineItemEventOrigin
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.matrix.ui.room.canSendMessageAsState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val BACK_PAGINATION_EVENT_LIMIT = 20
private const val BACK_PAGINATION_PAGE_SIZE = 50

class TimelinePresenter @AssistedInject constructor(
    private val timelineItemsFactory: TimelineItemsFactory,
    private val room: MatrixRoom,
    private val dispatchers: CoroutineDispatchers,
    private val appScope: CoroutineScope,
    @Assisted private val navigator: MessagesNavigator,
    private val verificationService: SessionVerificationService,
    private val encryptionService: EncryptionService,
    private val redactedVoiceMessageManager: RedactedVoiceMessageManager,
    private val sendPollResponseAction: SendPollResponseAction,
    private val endPollAction: EndPollAction,
) : Presenter<TimelineState> {
    @AssistedFactory
    interface Factory {
        fun create(navigator: MessagesNavigator): TimelinePresenter
    }

    private val timeline = room.timeline

    @Composable
    override fun present(): TimelineState {
        val localScope = rememberCoroutineScope()
        val highlightedEventId: MutableState<EventId?> = rememberSaveable {
            mutableStateOf(null)
        }

        val lastReadReceiptIndex = rememberSaveable { mutableIntStateOf(Int.MAX_VALUE) }
        val lastReadReceiptId = rememberSaveable { mutableStateOf<EventId?>(null) }

        val timelineItems by timelineItemsFactory.collectItemsAsState()
        val paginationState by timeline.paginationState.collectAsState()
        val syncUpdateFlow = room.syncUpdateFlow.collectAsState()
        val userHasPermissionToSendMessage by room.canSendMessageAsState(type = MessageEventType.ROOM_MESSAGE, updateKey = syncUpdateFlow.value)
        val userHasPermissionToSendReaction by room.canSendMessageAsState(type = MessageEventType.REACTION_SENT, updateKey = syncUpdateFlow.value)

        val prevMostRecentItemId = rememberSaveable { mutableStateOf<String?>(null) }
        val newItemState = remember { mutableStateOf(NewEventState.None) }

        val sessionVerifiedStatus by verificationService.sessionVerifiedStatus.collectAsState()
        val keyBackupState by encryptionService.backupStateStateFlow.collectAsState()

        val sessionState by remember {
            derivedStateOf {
                SessionState(
                    isSessionVerified = sessionVerifiedStatus == SessionVerifiedStatus.Verified,
                    isKeyBackupEnabled = keyBackupState == BackupState.ENABLED
                )
            }
        }

        val membersState by room.membersStateFlow.collectAsState()

        fun handleEvents(event: TimelineEvents) {
            when (event) {
                TimelineEvents.LoadMore -> localScope.paginateBackwards()
                is TimelineEvents.SetHighlightedEvent -> highlightedEventId.value = event.eventId
                is TimelineEvents.OnScrollFinished -> {
                    if (event.firstIndex == 0) {
                        newItemState.value = NewEventState.None
                    }
                    appScope.sendReadReceiptIfNeeded(
                        firstVisibleIndex = event.firstIndex,
                        timelineItems = timelineItems,
                        lastReadReceiptIndex = lastReadReceiptIndex,
                        lastReadReceiptId = lastReadReceiptId
                    )
                }
                is TimelineEvents.PollAnswerSelected -> appScope.launch {
                    sendPollResponseAction.execute(
                        pollStartId = event.pollStartId,
                        answerId = event.answerId
                    )
                }
                is TimelineEvents.PollEndClicked -> appScope.launch {
                    endPollAction.execute(
                        pollStartId = event.pollStartId,
                    )
                }
                is TimelineEvents.PollEditClicked ->
                    navigator.onEditPollClicked(event.pollStartId)
            }
        }

        LaunchedEffect(timelineItems.size) {
            computeNewItemState(timelineItems, prevMostRecentItemId, newItemState)
        }

        LaunchedEffect(Unit) {
            timeline
                .timelineItems
                .onEach {
                    timelineItemsFactory.replaceWith(
                        timelineItems = it,
                        roomMembers = membersState.roomMembers().orEmpty()
                    )
                }
                .onEach { timelineItems ->
                    if (timelineItems.isEmpty()) {
                        paginateBackwards()
                    }
                }
                .onEach(redactedVoiceMessageManager::onEachMatrixTimelineItem)
                .launchIn(this)
        }

        val timelineRoomInfo by remember {
            derivedStateOf {
                TimelineRoomInfo(
                    isDirect = room.isDirect,
                    userHasPermissionToSendMessage = userHasPermissionToSendMessage,
                    userHasPermissionToSendReaction = userHasPermissionToSendReaction,
                )
            }
        }
        return TimelineState(
            timelineRoomInfo = timelineRoomInfo,
            highlightedEventId = highlightedEventId.value,
            paginationState = paginationState,
            timelineItems = timelineItems,
            newEventState = newItemState.value,
            sessionState = sessionState,
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
        lastReadReceiptIndex: MutableState<Int>,
        lastReadReceiptId: MutableState<EventId?>,
    ) = launch(dispatchers.computation) {
        // Get last valid EventId seen by the user, as the first index might refer to a Virtual item
        val eventId = getLastEventIdBeforeOrAt(firstVisibleIndex, timelineItems)
        if (eventId != null && firstVisibleIndex <= lastReadReceiptIndex.value && eventId != lastReadReceiptId.value) {
            lastReadReceiptIndex.value = firstVisibleIndex
            lastReadReceiptId.value = eventId
            timeline.sendReadReceipt(eventId = eventId, receiptType = ReceiptType.READ)
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

    private fun CoroutineScope.paginateBackwards() = launch {
        timeline.paginateBackwards(BACK_PAGINATION_EVENT_LIMIT, BACK_PAGINATION_PAGE_SIZE)
    }
}
