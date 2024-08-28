/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.pinned.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.messages.impl.UserEventPermissions
import io.element.android.features.messages.impl.actionlist.ActionListPresenter
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.pinned.PinnedEventsTimelineProvider
import io.element.android.features.messages.impl.timeline.TimelineRoomInfo
import io.element.android.features.messages.impl.timeline.factories.TimelineItemsFactory
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.isDm
import io.element.android.libraries.matrix.api.room.powerlevels.canPinUnpin
import io.element.android.libraries.matrix.api.room.powerlevels.canRedactOther
import io.element.android.libraries.matrix.api.room.powerlevels.canRedactOwn
import io.element.android.libraries.matrix.api.room.roomMembers
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

class PinnedMessagesListPresenter @AssistedInject constructor(
    @Assisted private val navigator: PinnedMessagesListNavigator,
    private val room: MatrixRoom,
    private val timelineItemsFactory: TimelineItemsFactory,
    private val timelineProvider: PinnedEventsTimelineProvider,
    private val snackbarDispatcher: SnackbarDispatcher,
    actionListPresenterFactory: ActionListPresenter.Factory,
) : Presenter<PinnedMessagesListState> {

    @AssistedFactory
    interface Factory {
        fun create(navigator: PinnedMessagesListNavigator): PinnedMessagesListPresenter
    }

    private val actionListPresenter = actionListPresenterFactory.create(PinnedMessagesListTimelineActionPostProcessor)

    @Composable
    override fun present(): PinnedMessagesListState {
        val timelineRoomInfo = remember {
            TimelineRoomInfo(
                isDm = room.isDm,
                name = room.displayName,
                isMainTimeline = false,
                // We don't need to compute those values
                userHasPermissionToSendMessage = false,
                userHasPermissionToSendReaction = false,
                isCallOngoing = false,
            )
        }

        val syncUpdateFlow = room.syncUpdateFlow.collectAsState()
        val userEventPermissions by userEventPermissions(syncUpdateFlow.value)

        var pinnedMessageItems by remember {
            mutableStateOf<AsyncData<ImmutableList<TimelineItem>>>(AsyncData.Uninitialized)
        }

        PinnedMessagesListEffect(
            onItemsChange = { newItems ->
                pinnedMessageItems = newItems
            }
        )

        val coroutineScope = rememberCoroutineScope()
        fun handleEvents(event: PinnedMessagesListEvents) {
            when (event) {
                is PinnedMessagesListEvents.HandleAction -> coroutineScope.handleTimelineAction(event.action, event.event)
            }
        }

        return pinnedMessagesListState(
            timelineRoomInfo = timelineRoomInfo,
            userEventPermissions = userEventPermissions,
            timelineItems = pinnedMessageItems,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.handleTimelineAction(
        action: TimelineItemAction,
        targetEvent: TimelineItem.Event,
    ) = launch {
        when (action) {
            TimelineItemAction.Redact -> handleActionRedact(targetEvent)
            TimelineItemAction.ViewSource -> {
                navigator.onShowEventDebugInfoClick(targetEvent.eventId, targetEvent.debugInfo)
            }
            TimelineItemAction.Forward -> {
                targetEvent.eventId?.let { eventId ->
                    navigator.onForwardEventClick(eventId)
                }
            }
            TimelineItemAction.Unpin -> handleUnpinAction(targetEvent)
            TimelineItemAction.ViewInTimeline -> {
                targetEvent.eventId?.let { eventId ->
                    navigator.onViewInTimelineClick(eventId)
                }
            }
            else -> Unit
        }
    }

    private suspend fun handleUnpinAction(targetEvent: TimelineItem.Event) {
        if (targetEvent.eventId == null) return
        timelineProvider.invokeOnTimeline {
            unpinEvent(targetEvent.eventId)
                .onFailure {
                    Timber.e(it, "Failed to unpin event ${targetEvent.eventId}")
                    snackbarDispatcher.post(SnackbarMessage(CommonStrings.common_error))
                }
        }
    }

    private suspend fun handleActionRedact(event: TimelineItem.Event) {
        timelineProvider.invokeOnTimeline {
            redactEvent(eventId = event.eventId, transactionId = event.transactionId, reason = null)
                .onFailure { Timber.e(it) }
        }
    }

    @Composable
    private fun userEventPermissions(updateKey: Long): State<UserEventPermissions> {
        return produceState(UserEventPermissions.DEFAULT, key1 = updateKey) {
            value = UserEventPermissions(
                canSendMessage = false,
                canSendReaction = false,
                canRedactOwn = room.canRedactOwn().getOrElse { false },
                canRedactOther = room.canRedactOther().getOrElse { false },
                canPinUnpin = room.canPinUnpin().getOrElse { false },
            )
        }
    }

    @Composable
    private fun PinnedMessagesListEffect(onItemsChange: (AsyncData<ImmutableList<TimelineItem>>) -> Unit) {
        val updatedOnItemsChange by rememberUpdatedState(onItemsChange)

        val timelineState by timelineProvider.timelineStateFlow.collectAsState()

        LaunchedEffect(timelineState) {
            when (val asyncTimeline = timelineState) {
                AsyncData.Uninitialized -> flowOf(AsyncData.Uninitialized)
                is AsyncData.Failure -> flowOf(AsyncData.Failure(asyncTimeline.error))
                is AsyncData.Loading -> flowOf(AsyncData.Loading())
                is AsyncData.Success -> {
                    combine(asyncTimeline.data.timelineItems, room.membersStateFlow) { items, membersState ->
                        timelineItemsFactory.replaceWith(
                            timelineItems = items,
                            roomMembers = membersState.roomMembers().orEmpty()
                        )
                    }.launchIn(this)

                    timelineItemsFactory.timelineItems.map { timelineItems ->
                        AsyncData.Success(timelineItems)
                    }
                }
            }
                .onEach { items ->
                    updatedOnItemsChange(items)
                }
                .launchIn(this)
        }
    }

    @Composable
    private fun pinnedMessagesListState(
        timelineRoomInfo: TimelineRoomInfo,
        userEventPermissions: UserEventPermissions,
        timelineItems: AsyncData<ImmutableList<TimelineItem>>,
        eventSink: (PinnedMessagesListEvents) -> Unit
    ): PinnedMessagesListState {
        return when (timelineItems) {
            AsyncData.Uninitialized, is AsyncData.Loading -> PinnedMessagesListState.Loading
            is AsyncData.Failure -> PinnedMessagesListState.Failed
            is AsyncData.Success -> {
                if (timelineItems.data.isEmpty()) {
                    PinnedMessagesListState.Empty
                } else {
                    val actionListState = actionListPresenter.present()
                    PinnedMessagesListState.Filled(
                        timelineRoomInfo = timelineRoomInfo,
                        userEventPermissions = userEventPermissions,
                        timelineItems = timelineItems.data,
                        actionListState = actionListState,
                        eventSink = eventSink
                    )
                }
            }
        }
    }
}
