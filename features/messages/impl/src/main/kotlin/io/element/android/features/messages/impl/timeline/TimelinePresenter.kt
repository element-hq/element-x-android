/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.features.location.api.live.ActiveLiveLocationShareManager
import io.element.android.features.messages.impl.MessagesNavigator
import io.element.android.features.messages.impl.UserEventPermissions
import io.element.android.features.messages.impl.crypto.sendfailure.resolve.ResolveVerifiedUserSendFailureEvent
import io.element.android.features.messages.impl.crypto.sendfailure.resolve.ResolveVerifiedUserSendFailureState
import io.element.android.features.messages.impl.timeline.components.MessageShieldData
import io.element.android.features.messages.impl.timeline.factories.TimelineItemsFactory
import io.element.android.features.messages.impl.timeline.factories.TimelineItemsFactoryConfig
import io.element.android.features.messages.impl.timeline.model.NewEventState
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemReadMarkerModel
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemTypingNotificationModel
import io.element.android.features.messages.impl.typing.TypingNotificationState
import io.element.android.features.messages.impl.userEventPermissions
import io.element.android.features.messages.impl.voicemessages.timeline.RedactedVoiceMessageManager
import io.element.android.features.poll.api.actions.EndPollAction
import io.element.android.features.poll.api.actions.SendPollResponseAction
import io.element.android.features.roomcall.api.RoomCallState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.core.asEventId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.powerlevels.permissionsAsState
import io.element.android.libraries.matrix.api.room.roomMembers
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.event.TimelineItemEventOrigin
import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import io.element.android.services.analytics.api.AnalyticsLongRunningTransaction.DisplayFirstTimelineItems
import io.element.android.services.analytics.api.AnalyticsLongRunningTransaction.NotificationToMessage
import io.element.android.services.analytics.api.AnalyticsLongRunningTransaction.OpenRoom
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.api.finishLongRunningTransaction
import io.element.android.services.analyticsproviders.api.AnalyticsUserData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

const val FOCUS_ON_PINNED_EVENT_DEBOUNCE_DURATION_IN_MILLIS = 200L

@AssistedInject
class TimelinePresenter(
    timelineItemsFactoryCreator: TimelineItemsFactory.Creator,
    private val room: JoinedRoom,
    private val dispatchers: CoroutineDispatchers,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
    @Assisted private val navigator: MessagesNavigator,
    private val redactedVoiceMessageManager: RedactedVoiceMessageManager,
    private val sendPollResponseAction: SendPollResponseAction,
    private val endPollAction: EndPollAction,
    private val sessionPreferencesStore: SessionPreferencesStore,
    @Assisted private val timelineController: TimelineController,
    private val timelineItemIndexer: TimelineItemIndexer = TimelineItemIndexer(),
    private val resolveVerifiedUserSendFailurePresenter: Presenter<ResolveVerifiedUserSendFailureState>,
    private val typingNotificationPresenter: Presenter<TypingNotificationState>,
    private val roomCallStatePresenter: Presenter<RoomCallState>,
    private val featureFlagService: FeatureFlagService,
    private val analyticsService: AnalyticsService,
    private val liveLocationShareManager: ActiveLiveLocationShareManager,
    private val markAsFullyRead: MarkAsFullyRead,
) : Presenter<TimelineState> {
    private val tag = "TimelinePresenter"

    @AssistedFactory
    interface Factory {
        fun create(
            timelineController: TimelineController,
            navigator: MessagesNavigator
        ): TimelinePresenter
    }

    private val timelineItemsFactory: TimelineItemsFactory = timelineItemsFactoryCreator.create(
        config = TimelineItemsFactoryConfig(
            computeReadReceipts = true,
            computeReactions = true,
        )
    )
    private var timelineItems by mutableStateOf<ImmutableList<TimelineItem>>(persistentListOf())

    private val focusRequestState: MutableState<FocusRequestState> = mutableStateOf(FocusRequestState.None)

    @Composable
    override fun present(): TimelineState {
        LaunchedEffect(Unit) {
            val parent = analyticsService.getLongRunningTransaction(OpenRoom)
            analyticsService.startLongRunningTransaction(DisplayFirstTimelineItems, parent)
        }

        val localScope = rememberCoroutineScope()

        val timelineMode = remember { timelineController.mainTimelineMode() }

        val lastReadReceiptId = rememberSaveable { mutableStateOf<EventId?>(null) }

        val roomInfo by room.roomInfoFlow.collectAsState()

        val prevMostRecentItemId = rememberSaveable { mutableStateOf<UniqueId?>(null) }

        val newEventState = remember { mutableStateOf<NewEventState>(NewEventState.None) }
        val messageShieldDialogData: MutableState<MessageShieldData?> = remember { mutableStateOf(null) }

        // Forces [JumpToUnreadState.Hidden] until the next RoomInfo push. Set after a
        // [TimelineEvent.MarkAllAsRead] await completes so the FAB hides without waiting for
        // the SDK to push a refreshed fully-read marker; the after-await ordering means any
        // RoomInfo update racing the mark-as-read call has already landed and can't undo this.
        val suppressJumpToUnread = remember { mutableStateOf(false) }

        val resolveVerifiedUserSendFailureState = resolveVerifiedUserSendFailurePresenter.present()
        val isLive by remember {
            timelineController.isLive()
        }.collectAsState(initial = true)

        val displayThreadSummaries by produceState(false) {
            value = featureFlagService.isFeatureEnabled(FeatureFlags.Threads)
        }
        val displayJumpToUnread by produceState(false) {
            value = featureFlagService.isFeatureEnabled(FeatureFlags.JumpToUnread)
        }

        fun handleEvent(event: TimelineEvent) {
            when (event) {
                is TimelineEvent.LoadMore -> {
                    if (event.direction == Timeline.PaginationDirection.FORWARDS && timelineMode is Timeline.Mode.Thread) {
                        // Do not paginate forwards in thread mode, as it's not supported
                        return
                    }
                    localScope.launch {
                        timelineController.paginate(direction = event.direction)
                    }
                }
                is TimelineEvent.OnScrollFinished -> {
                    if (isLive) {
                        if (event.firstIndex == 0) {
                            newEventState.value = NewEventState.None
                        }
                        Timber.tag(tag).d("## sendReadReceiptIfNeeded firstVisibleIndex: ${event.firstIndex}")
                        sessionCoroutineScope.launch {
                            val sendPublicReadReceipts = sessionPreferencesStore.isSendPublicReadReceiptsEnabled().first()
                            sendReadReceiptIfNeeded(
                                firstVisibleIndex = event.firstIndex,
                                timelineItems = timelineItems,
                                lastReadReceiptId = lastReadReceiptId,
                                readReceiptType = if (sendPublicReadReceipts) ReceiptType.READ else ReceiptType.READ_PRIVATE,
                            )
                        }
                    } else {
                        newEventState.value = NewEventState.None
                    }
                }
                is TimelineEvent.SelectPollAnswer -> sessionCoroutineScope.launch {
                    timelineController.invokeOnCurrentTimeline {
                        sendPollResponseAction.execute(
                            timeline = this,
                            pollStartId = event.pollStartId,
                            answerId = event.answerId
                        )
                    }
                }
                is TimelineEvent.EndPoll -> sessionCoroutineScope.launch {
                    timelineController.invokeOnCurrentTimeline {
                        endPollAction.execute(
                            timeline = this,
                            pollStartId = event.pollStartId,
                        )
                    }
                }
                is TimelineEvent.EditPoll -> {
                    navigator.navigateToEditPoll(event.pollStartId)
                }
                is TimelineEvent.StopLiveLocationShare -> sessionCoroutineScope.launch {
                    liveLocationShareManager.stopShare(room.roomId)
                }
                is TimelineEvent.FocusOnEvent -> sessionCoroutineScope.launch {
                    focusRequestState.value = FocusRequestState.Requested(event.eventId, event.debounce)
                    delay(event.debounce)
                    Timber.tag(tag).d("Started focus on ${event.eventId}")
                    focusOnEvent(event.eventId, focusRequestState)
                }.start()
                is TimelineEvent.OnFocusEventRender -> {
                    // If there was a pending 'notification tap opens timeline' transaction, finish it now we're focused in the required event
                    analyticsService.finishLongRunningTransaction(NotificationToMessage)

                    focusRequestState.value = focusRequestState.value.onFocusEventRender()
                }
                is TimelineEvent.ClearFocusRequestState -> {
                    focusRequestState.value = FocusRequestState.None
                }
                is TimelineEvent.JumpToLive -> {
                    timelineController.focusOnLive()
                }
                TimelineEvent.HideShieldDialog -> messageShieldDialogData.value = null
                TimelineEvent.MarkAllAsRead -> sessionCoroutineScope.launch {
                    val latestEventId = room.liveTimeline.getLatestEventId().getOrElse {
                        Timber.tag(tag).w(it, "Failed to get latest event id to mark as fully read")
                        null
                    } ?: return@launch
                    markAsFullyRead(room.roomId, latestEventId)
                    suppressJumpToUnread.value = true
                }
                is TimelineEvent.ShowShieldDialog -> messageShieldDialogData.value = event.messageShieldData
                is TimelineEvent.ComputeVerifiedUserSendFailure -> {
                    resolveVerifiedUserSendFailureState.eventSink(ResolveVerifiedUserSendFailureEvent.ComputeForMessage(event.event))
                }
                is TimelineEvent.NavigateToPredecessorOrSuccessorRoom -> {
                    // Navigate to the predecessor or successor room
                    val serverNames = calculateServerNamesForRoom(room)
                    navigator.navigateToRoom(event.roomId, null, serverNames)
                }
                is TimelineEvent.OpenThread -> {
                    navigator.navigateToThread(
                        threadRootId = event.threadRootEventId,
                        focusedEventId = event.focusedEvent,
                    )
                }
            }
        }

        LaunchedEffect(Unit) {
            timelineItemsFactory.timelineItems
                .onEach { newTimelineItems ->
                    timelineItemIndexer.process(newTimelineItems)
                    timelineItems = newTimelineItems

                    analyticsService.run {
                        finishLongRunningTransaction(DisplayFirstTimelineItems)
                        finishLongRunningTransaction(OpenRoom)
                    }
                }
                .launchIn(this)

            combine(
                timelineController.timelineItems(),
                room.membersStateFlow,
                sessionPreferencesStore.isRenderReadReceiptsEnabled(),
            ) { items, membersState, renderReadReceipts ->
                val parent = analyticsService.getLongRunningTransaction(DisplayFirstTimelineItems)
                val transaction = parent?.startChild("timelineItemsFactory.replaceWith", "Processing timeline items")
                transaction?.putExtraData(AnalyticsUserData.TIMELINE_ITEM_COUNT, items.count().toString())
                timelineItemsFactory.replaceWith(
                    timelineItems = items,
                    roomMembers = membersState.roomMembers().orEmpty(),
                    renderReadReceipts = renderReadReceipts,
                )
                transaction?.finish()
                items
            }
                .onEach(redactedVoiceMessageManager::onEachMatrixTimelineItem)
                .flowOn(dispatchers.computation)
                .launchIn(this)
        }

        LaunchedEffect(timelineItems.size) {
            computeNewItemState(timelineItems, prevMostRecentItemId, newEventState)
        }

        // Keyed on the full [timelineItems] reference (not just .size) so we re-scan when the
        // read marker advances in place — the SDK swaps the marker virtual item to a new position
        // without changing the list length, e.g. when [markRoomAsFullyRead] is sent while at the
        // bottom of the room.
        //
        // The state has three shapes:
        //  - InWindow: the SDK has materialised a virtual ReadMarker item in the loaded window;
        //    tapping the FAB smoothly scrolls to its index.
        //  - OutOfWindow: the marker event is older than the loaded window, so the SDK gives us
        //    only the event id via RoomInfo.fullyReadEventId; tapping triggers a focused-event
        //    load via the existing TimelineEvent.FocusOnEvent path.
        //  - Hidden: feature flag off, no marker, caught-up (marker loaded but no virtual item),
        //    or initial load (no items yet).
        val jumpToUnread = remember { mutableStateOf<JumpToUnreadState>(JumpToUnreadState.Hidden) }
        // The SDK is authoritative again once it pushes a new fully-read marker, so drop the
        // post-mark-as-read suppression and let the recompute below pick up the new value.
        LaunchedEffect(roomInfo.fullyReadEventId) {
            suppressJumpToUnread.value = false
        }
        LaunchedEffect(
            timelineItems.map { it.identifier() },
            displayJumpToUnread,
            roomInfo.fullyReadEventId,
            roomInfo.numUnreadMessages,
            suppressJumpToUnread.value,
        ) {
            if (!displayJumpToUnread || suppressJumpToUnread.value) {
                jumpToUnread.value = JumpToUnreadState.Hidden
                return@LaunchedEffect
            }
            val items = timelineItems
            val fullyReadEventId = roomInfo.fullyReadEventId
            val hasUnreadMessages = roomInfo.numUnreadMessages > 0
            val markerIndex = withContext(dispatchers.computation) {
                items.indexOfFirst {
                    (it as? TimelineItem.Virtual)?.model is TimelineItemReadMarkerModel
                }
            }
            jumpToUnread.value = when {
                markerIndex >= 0 -> JumpToUnreadState.InWindow(markerIndex)
                // Out-of-window only when there is genuinely unread *displayable* content
                // (numUnreadMessages counts "interesting" messages, never state/hidden events) AND
                // the marker event isn't merely an in-window item we don't render. isKnown is the
                // cheap display-index check; isEventLoaded falls back to the SDK to tell
                // "in window but not displayed" apart from "genuinely out of window".
                fullyReadEventId != null &&
                    hasUnreadMessages &&
                    items.isNotEmpty() &&
                    !timelineItemIndexer.isKnown(fullyReadEventId) &&
                    !timelineController.activeTimelineFlow().value.isEventLoaded(fullyReadEventId) ->
                    JumpToUnreadState.OutOfWindow(fullyReadEventId)
                else -> JumpToUnreadState.Hidden
            }
        }

        // Keyed on the full [timelineItems] reference (not just .size) so we re-resolve the index
        // when a focused timeline loads with the same item count as the window it replaced — e.g.
        // jumping to an out-of-window read marker in a busy room, where both windows fill to the
        // same page size. With .size as the key the effect wouldn't re-run, the focused event's
        // index would stay unresolved, and the scroll would never fire until a second tap.
        LaunchedEffect(timelineItems.map { it.identifier() }, focusRequestState.value) {
            val currentFocusRequestState = focusRequestState.value
            if (currentFocusRequestState is FocusRequestState.Success && !currentFocusRequestState.rendered) {
                val eventId = currentFocusRequestState.eventId
                if (timelineItemIndexer.isKnown(eventId)) {
                    val index = timelineItemIndexer.indexOf(eventId)
                    focusRequestState.value = FocusRequestState.Success(eventId = eventId, index = index)
                } else {
                    Timber.w("Unknown timeline item for focused item, can't render focus")
                }
            }
        }

        val typingNotificationState = typingNotificationPresenter.present()
        val roomCallState = roomCallStatePresenter.present()
        val userEventPermissions by room.permissionsAsState(UserEventPermissions.DEFAULT) { perms ->
            perms.userEventPermissions()
        }
        val timelineRoomInfo by remember(typingNotificationState, roomCallState, roomInfo) {
            derivedStateOf {
                TimelineRoomInfo(
                    name = roomInfo.name,
                    isDm = roomInfo.isDm,
                    userHasPermissionToSendMessage = userEventPermissions.canSendMessage,
                    userHasPermissionToSendReaction = userEventPermissions.canSendReaction,
                    roomCallState = roomCallState,
                    pinnedEventIds = roomInfo.pinnedEventIds,
                    typingNotificationState = typingNotificationState,
                    predecessorRoom = room.predecessorRoom(),
                )
            }
        }

        LaunchedEffect(focusRequestState.value) {
            Timber.tag(tag).d("Timeline: $timelineMode | focus state: ${focusRequestState.value}")
        }

        return TimelineState(
            timelineItems = timelineItems,
            timelineMode = timelineMode,
            timelineRoomInfo = timelineRoomInfo,
            newEventState = newEventState.value,
            isLive = isLive,
            focusRequestState = focusRequestState.value,
            messageShieldDialogData = messageShieldDialogData.value,
            resolveVerifiedUserSendFailureState = resolveVerifiedUserSendFailureState,
            displayThreadSummaries = displayThreadSummaries,
            displayJumpToUnread = displayJumpToUnread,
            jumpToUnread = jumpToUnread.value,
            eventSink = ::handleEvent,
        )
    }

    private suspend fun focusOnEvent(
        eventId: EventId,
        focusRequestState: MutableState<FocusRequestState>,
    ) {
        if (timelineItemIndexer.isKnown(eventId)) {
            val index = timelineItemIndexer.indexOf(eventId)
            focusRequestState.value = FocusRequestState.Success(eventId = eventId, index = index)
            return
        }

        Timber.tag(tag).d("Event $eventId not found in the loaded timeline, loading a focused timeline")
        focusRequestState.value = FocusRequestState.Loading(eventId = eventId)

        val threadId = room.threadRootIdForEvent(eventId).getOrElse {
            focusRequestState.value = FocusRequestState.Failure(it)
            return
        }

        if (timelineController.mainTimelineMode() is Timeline.Mode.Thread && threadId == null) {
            // We are in a thread timeline, and the event isn't part of a thread, we need to navigate back to the room
            focusRequestState.value = FocusRequestState.None
            navigator.navigateToRoom(room.roomId, eventId, calculateServerNamesForRoom(room))
        } else {
            Timber.tag(tag).d("Focusing on event $eventId - thread $threadId")
            timelineController.focusOnEvent(eventId, threadId)
                .onSuccess { result ->
                    when (result) {
                        is EventFocusResult.FocusedOnLive -> {
                            focusRequestState.value = FocusRequestState.Success(eventId = eventId)
                        }
                        is EventFocusResult.IsInThread -> {
                            val currentThreadId = (timelineController.mainTimelineMode() as? Timeline.Mode.Thread)?.threadRootId
                            if (currentThreadId == result.threadId) {
                                // It's the same thread, we just focus on the event
                                focusRequestState.value = FocusRequestState.Success(eventId = eventId)
                            } else {
                                focusRequestState.value = FocusRequestState.Success(eventId = result.threadId.asEventId())
                                // It's part of a thread we're not in, let's open it in another timeline
                                navigator.navigateToThread(result.threadId, eventId)
                            }
                        }
                    }
                }
                .onFailure {
                    focusRequestState.value = FocusRequestState.Failure(it)
                }
        }
    }

    /**
     * This method compute the hasNewItem state passed as a [MutableState] each time the timeline items size changes.
     * Basically, if we got new timeline event from sync or local, either from us or another user, we update the state so we tell we have new items.
     * The state never goes back to None from this method, but need to be reset from somewhere else.
     */
    private suspend fun computeNewItemState(
        timelineItems: ImmutableList<TimelineItem>,
        prevMostRecentItemId: MutableState<UniqueId?>,
        newEventState: MutableState<NewEventState>,
    ) = withContext(dispatchers.computation) {
        // FromMe is prioritized over FromOther, so skip if we already have a FromMe
        if (newEventState.value == NewEventState.FromMe) {
            return@withContext
        }
        val newMostRecentItem = timelineItems.firstOrNull {
            // Ignore typing item
            (it as? TimelineItem.Virtual)?.model !is TimelineItemTypingNotificationModel
        }
        val prevMostRecentItemIdValue = prevMostRecentItemId.value
        val newMostRecentItemId = newMostRecentItem?.identifier()
        val hasNewEvent = prevMostRecentItemIdValue != null &&
            newMostRecentItem is TimelineItem.Event &&
            newMostRecentItem.origin != TimelineItemEventOrigin.PAGINATION &&
            newMostRecentItemId != prevMostRecentItemIdValue

        if (hasNewEvent) {
            // Scroll to bottom if the new event is from me, even if sent from another device
            newEventState.value = if (newMostRecentItem.isMine) NewEventState.FromMe else NewEventState.FromOther
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
            timelineController.invokeOnCurrentTimeline {
                markAsRead(receiptType = readReceiptType)
            }
        } else {
            // Get last valid EventId seen by the user, as the first index might refer to a Virtual item
            val eventId = getLastEventIdBeforeOrAt(firstVisibleIndex, timelineItems)
            if (eventId != null && eventId != lastReadReceiptId.value) {
                lastReadReceiptId.value = eventId
                timelineController.invokeOnCurrentTimeline {
                    sendReadReceipt(eventId = eventId, receiptType = readReceiptType)
                }
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

private fun FocusRequestState.onFocusEventRender(): FocusRequestState {
    return when (this) {
        is FocusRequestState.Success -> copy(rendered = true)
        else -> this
    }
}

// Workaround for not having the server names available, get possible server names from the user ids of the room members
private fun calculateServerNamesForRoom(room: JoinedRoom): List<String> {
    // If we have no room members, return right ahead
    val serverNames = room.membersStateFlow.value.roomMembers() ?: return emptyList()

    // Otherwise get the three most common server names from the user ids of the room members
    return serverNames
        .mapNotNull { it.userId.domainName }
        .groupingBy { it }
        .eachCount()
        .let { map ->
            map.keys.sortedByDescending { map[it] }
        }
        .take(3)
}
