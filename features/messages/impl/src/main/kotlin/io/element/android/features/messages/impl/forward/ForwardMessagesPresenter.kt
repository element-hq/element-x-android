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

package io.element.android.features.messages.impl.forward

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.timeline.TimelineProvider
import io.element.android.libraries.matrix.api.timeline.getActiveTimeline
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ForwardMessagesPresenter @AssistedInject constructor(
    @Assisted eventId: String,
    @Assisted private val timelineProvider: TimelineProvider,
    private val appCoroutineScope: CoroutineScope,
) : Presenter<ForwardMessagesState> {
    private val eventId: EventId = EventId(eventId)

    @AssistedFactory
    interface Factory {
        fun create(eventId: String, timelineProvider: TimelineProvider): ForwardMessagesPresenter
    }

    private val forwardingActionState: MutableState<AsyncAction<List<RoomId>>> = mutableStateOf(AsyncAction.Uninitialized)

    fun onRoomSelected(roomIds: List<RoomId>) {
        appCoroutineScope.forwardEvent(eventId, roomIds.toPersistentList(), forwardingActionState)
    }

    @Composable
    override fun present(): ForwardMessagesState {
        fun handleEvents(event: ForwardMessagesEvents) {
            when (event) {
                ForwardMessagesEvents.ClearError -> forwardingActionState.value = AsyncAction.Uninitialized
            }
        }

        return ForwardMessagesState(
            forwardAction = forwardingActionState.value,
            eventSink = { handleEvents(it) }
        )
    }

    private fun CoroutineScope.forwardEvent(
        eventId: EventId,
        roomIds: ImmutableList<RoomId>,
        isForwardMessagesState: MutableState<AsyncAction<List<RoomId>>>,
    ) = launch {
        suspend {
            timelineProvider.getActiveTimeline().forwardEvent(eventId, roomIds).getOrThrow()
            roomIds
        }.runCatchingUpdatingState(isForwardMessagesState)
    }
}
