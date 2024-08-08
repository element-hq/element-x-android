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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import io.element.android.features.messages.impl.pinned.PinnedEventsTimelineProvider
import io.element.android.features.messages.impl.timeline.TimelineRoomInfo
import io.element.android.features.messages.impl.timeline.factories.TimelineItemsFactory
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.isDm
import io.element.android.libraries.matrix.api.room.roomMembers
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class PinnedMessagesListPresenter @Inject constructor(
    private val room: MatrixRoom,
    private val timelineItemsFactory: TimelineItemsFactory,
    private val timelineProvider: PinnedEventsTimelineProvider,
) : Presenter<PinnedMessagesListState> {

    @Composable
    override fun present(): PinnedMessagesListState {
        val timelineRoomInfo = remember {
            TimelineRoomInfo(
                isDm = room.isDm,
                name = room.displayName,
                // We don't need to compute those values
                userHasPermissionToSendMessage = false,
                userHasPermissionToSendReaction = false,
                isCallOngoing = false,
            )
        }

        var pinnedMessageItems by remember {
            mutableStateOf<AsyncData<ImmutableList<TimelineItem>>>(AsyncData.Uninitialized)
        }

        PinnedMessagesListEffect(
            onItemsChange = { newItems ->
                pinnedMessageItems = newItems
            }
        )

        fun handleEvents(event: PinnedMessagesListEvents) {
        }

        return pinnedMessagesListState(
            timelineRoomInfo = timelineRoomInfo,
            timelineItems = pinnedMessageItems,
            eventSink = ::handleEvents
        )
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

    private fun pinnedMessagesListState(
        timelineRoomInfo: TimelineRoomInfo,
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
                    PinnedMessagesListState.Filled(
                        timelineRoomInfo = timelineRoomInfo,
                        timelineItems = timelineItems.data,
                        eventSink = eventSink
                    )
                }
            }
        }
    }
}
