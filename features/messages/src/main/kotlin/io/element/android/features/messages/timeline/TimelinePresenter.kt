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

package io.element.android.features.messages.timeline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import io.element.android.features.messages.timeline.factories.TimelineItemsFactory
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.core.EventId
import io.element.android.libraries.matrix.room.MatrixRoom
import io.element.android.libraries.matrix.timeline.MatrixTimeline
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val backPaginationEventLimit = 20
private const val backPaginationPageSize = 50

class TimelinePresenter @Inject constructor(
    private val timelineItemsFactory: TimelineItemsFactory,
    room: MatrixRoom,
) : Presenter<TimelineState> {

    private val timeline = room.timeline()

    @Composable
    override fun present(): TimelineState {
        val localCoroutineScope = rememberCoroutineScope()
        val highlightedEventId: MutableState<EventId?> = rememberSaveable {
            mutableStateOf(null)
        }
        val timelineItems = timelineItemsFactory
            .flow()
            .collectAsState()

        val paginationState = timeline
            .paginationState()
            .collectAsState()

        fun handleEvents(event: TimelineEvents) {
            when (event) {
                TimelineEvents.LoadMore -> localCoroutineScope.loadMore(paginationState.value)
                is TimelineEvents.SetHighlightedEvent -> highlightedEventId.value = event.eventId
            }
        }

        LaunchedEffect(Unit) {
            timeline
                .timelineItems()
                .onEach(timelineItemsFactory::replaceWith)
                .onEach { timelineItems ->
                    if (timelineItems.isEmpty()) {
                        loadMore(paginationState.value)
                    }
                }
                .launchIn(this)
        }

        DisposableEffect(Unit) {
            timeline.initialize()
            onDispose {
                timeline.dispose()
            }
        }

        return TimelineState(
            highlightedEventId = highlightedEventId.value,
            paginationState = paginationState.value,
            timelineItems = timelineItems.value.toImmutableList(),
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.loadMore(paginationState: MatrixTimeline.PaginationState) = launch {
        if (paginationState.canBackPaginate && !paginationState.isBackPaginating) {
            timeline.paginateBackwards(backPaginationEventLimit, backPaginationPageSize)
        } else {
            Timber.v("Can't back paginate as paginationState = $paginationState")
        }
    }
}
