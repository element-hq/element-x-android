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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import io.element.android.features.messages.impl.timeline.TimelineRoomInfo
import io.element.android.features.messages.impl.timeline.factories.TimelineItemsFactory
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.isDm
import io.element.android.libraries.matrix.api.room.roomMembers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import javax.inject.Inject

class PinnedMessagesListPresenter @Inject constructor(
    private val room: MatrixRoom,
    private val timelineItemsFactory: TimelineItemsFactory,
) : Presenter<PinnedMessagesListState> {

    @Composable
    override fun present(): PinnedMessagesListState {
        val timelineItems by timelineItemsFactory.collectItemsAsState()
        val timelineRoomInfo = remember {
            TimelineRoomInfo(
                isDm = room.isDm,
                name = room.displayName,
                userHasPermissionToSendMessage = false,
                userHasPermissionToSendReaction = false,
                isCallOngoing = false,
            )
        }
        LaunchedEffect(Unit) {
            val timeline = room.pinnedEventsTimeline().getOrNull() ?: return@LaunchedEffect
            combine(timeline.timelineItems, room.membersStateFlow) { items, membersState ->
                timelineItemsFactory.replaceWith(
                    timelineItems = items,
                    roomMembers = membersState.roomMembers().orEmpty()
                )
                items
            }.launchIn(this)
        }

        fun handleEvents(event: PinnedMessagesListEvents) {
        }

        return PinnedMessagesListState(
            timelineRoomInfo = timelineRoomInfo,
            timelineItems = timelineItems,
            eventSink = ::handleEvents
        )
    }
}
