/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.forward.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.core.extensions.flatMap
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CreateTimelineParams
import io.element.android.libraries.matrix.api.room.JoinedRoom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@AssistedInject
class ForwardMessagesPresenter(
    @Assisted private val eventIds: List<EventId>,
    private val room: JoinedRoom,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
) : Presenter<ForwardMessagesState> {
    @AssistedFactory
    fun interface Factory {
        fun create(eventIds: List<EventId>): ForwardMessagesPresenter
    }

    private val forwardingActionState: MutableState<AsyncAction<List<RoomId>>> = mutableStateOf(AsyncAction.Uninitialized)

    fun onRoomSelected(roomIds: List<RoomId>) {
        sessionCoroutineScope.forwardEvents(eventIds, roomIds)
    }

    @Composable
    override fun present(): ForwardMessagesState {
        fun handleEvent(event: ForwardMessagesEvent) {
            when (event) {
                ForwardMessagesEvent.ClearError -> forwardingActionState.value = AsyncAction.Uninitialized
            }
        }

        return ForwardMessagesState(
            forwardAction = forwardingActionState.value,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.forwardEvents(
        eventIds: List<EventId>,
        roomIds: List<RoomId>,
    ) = launch {
        suspend {
            // The events can come from any timeline (live, detached, thread, pinned, media) and can be far apart,
            // so use a dedicated timeline focused on each event, sequentially to keep the order.
            eventIds.forEach { eventId ->
                room.createTimeline(CreateTimelineParams.Focused(eventId))
                    .flatMap { timeline ->
                        timeline.use { it.forwardEvents(listOf(eventId), roomIds) }
                    }
                    .onFailure {
                        Timber.e(it, "Error while forwarding event $eventId")
                    }
                    .getOrThrow()
            }
            roomIds
        }.runCatchingUpdatingState(forwardingActionState)
    }
}
