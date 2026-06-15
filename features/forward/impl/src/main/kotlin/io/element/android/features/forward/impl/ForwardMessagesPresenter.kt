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
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.timeline.TimelineProvider
import io.element.android.libraries.matrix.api.timeline.getActiveTimeline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

private const val FORWARD_THROTTLE_MS = 150L

@AssistedInject
class ForwardMessagesPresenter(
    @Assisted eventIds: List<String>,
    @Assisted private val timelineProvider: TimelineProvider,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
) : Presenter<ForwardMessagesState> {
    private val eventIds: List<EventId> = eventIds.map(::EventId)

    @AssistedFactory
    fun interface Factory {
        fun create(eventIds: List<String>, timelineProvider: TimelineProvider): ForwardMessagesPresenter
    }

    private val forwardingActionState: MutableState<AsyncAction<List<RoomId>>> = mutableStateOf(AsyncAction.Uninitialized)

    fun onRoomSelected(roomIds: List<RoomId>) {
        sessionCoroutineScope.forwardEvents(eventIds, roomIds)
    }

    @Composable
    override fun present(): ForwardMessagesState {
        fun handleEvent(event: ForwardMessagesEvents) {
            when (event) {
                ForwardMessagesEvents.ClearError -> forwardingActionState.value = AsyncAction.Uninitialized
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
            val timeline = timelineProvider.getActiveTimeline()
            var failures = 0
            var lastError: Throwable? = null
            for ((index, eventId) in eventIds.withIndex()) {
                val result = timeline.forwardEvent(eventId, roomIds)
                if (result.isFailure) {
                    failures += 1
                    lastError = result.exceptionOrNull()
                    Timber.e(lastError, "Error forwarding event $eventId ($failures failed so far)")
                }
                // Throttle between sends so a large batch stays under the homeserver rate limit.
                if (index < eventIds.lastIndex) delay(FORWARD_THROTTLE_MS)
            }
            // Only surface an error if every forward failed; partial success still closes the screen.
            if (failures == eventIds.size && lastError != null) throw lastError
            roomIds
        }.runCatchingUpdatingState(forwardingActionState)
    }
}
