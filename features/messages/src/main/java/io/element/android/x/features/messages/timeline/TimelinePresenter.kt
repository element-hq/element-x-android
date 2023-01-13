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

package io.element.android.x.features.messages.timeline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import io.element.android.x.architecture.Presenter
import io.element.android.x.matrix.MatrixClient
import io.element.android.x.matrix.core.EventId
import io.element.android.x.matrix.room.MatrixRoom
import io.element.android.x.matrix.timeline.MatrixTimeline
import io.element.android.x.matrix.timeline.MatrixTimelineItem
import io.element.android.x.matrix.ui.MatrixItemHelper
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGINATION_COUNT = 50

class TimelinePresenter @Inject constructor(
    private val appCoroutineScope: CoroutineScope,
    private val client: MatrixClient,
    private val room: MatrixRoom
) : Presenter<TimelineState> {

    private val timeline = room.timeline()
    private val matrixItemHelper = MatrixItemHelper(client)
    private val timelineItemsFactory =
        TimelineItemsFactory(matrixItemHelper, room, Dispatchers.Default)

    private class TimelineCallback(private val coroutineScope: CoroutineScope, private val timelineItemsFactory: TimelineItemsFactory) : MatrixTimeline.Callback {
        override fun onPushedTimelineItem(timelineItem: MatrixTimelineItem) {
            coroutineScope.launch {
                timelineItemsFactory.pushItem(timelineItem)
            }
        }
    }

    @Composable
    override fun present(): TimelineState {
        val localCoroutineScope = rememberCoroutineScope()
        val hasMoreToLoad = rememberSaveable {
            mutableStateOf(timeline.hasMoreToLoad)
        }
        val highlightedEventId: MutableState<EventId?> = rememberSaveable {
            mutableStateOf(null)
        }
        val timelineItems = timelineItemsFactory
            .flow()
            .collectAsState(emptyList())

        fun handleEvents(event: TimelineEvents) {
            when (event) {
                TimelineEvents.LoadMore -> localCoroutineScope.loadMore(hasMoreToLoad)
                is TimelineEvents.SetHighlightedEvent -> highlightedEventId.value = event.eventId
            }
        }

        LaunchedEffect(Unit) {
            timeline
                .timelineItems()
                .onEach(timelineItemsFactory::replaceWith)
                .launchIn(this)
        }

        DisposableEffect(Unit) {
            timeline.callback = TimelineCallback(localCoroutineScope, timelineItemsFactory)
            timeline.initialize()
            onDispose {
                timeline.callback = null
                timeline.dispose()
            }
        }

        return TimelineState(
            highlightedEventId = highlightedEventId.value,
            timelineItems = timelineItems.value.toImmutableList(),
            hasMoreToLoad = hasMoreToLoad.value,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.loadMore(hasMoreToLoad: MutableState<Boolean>) = launch {
        timeline.paginateBackwards(PAGINATION_COUNT)
        hasMoreToLoad.value = timeline.hasMoreToLoad
    }
}
