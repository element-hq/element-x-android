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

package io.element.android.features.poll.impl.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.timeline.MatrixTimeline
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class PollHistoryPresenter @AssistedInject constructor(
    @Assisted private val pollHistory: MatrixTimeline,
) : Presenter<PollHistoryState> {

    @AssistedFactory
    interface Factory {
        fun create(
            pollHistory: MatrixTimeline,
        ): PollHistoryPresenter
    }

    @Composable
    override fun present(): PollHistoryState {
        val paginationState by pollHistory.paginationState.collectAsState()
        val timelineItemsFlow = remember {
            pollHistory.timelineItems.map { items ->
                items.filterIsInstance<MatrixTimelineItem.Event>()
                    .map { it.event.content }
                    .filterIsInstance<PollContent>()
                    .reversed()
            }.onEach {
                if (it.isEmpty()) pollHistory.paginateBackwards(20, 50)
            }
        }
        val items by timelineItemsFlow.collectAsState(initial = emptyList())

        fun handleEvents(event: PollHistoryEvents) {
            when (event) {
                is PollHistoryEvents.History -> Unit // TODO which events to handle?
            }
        }

        return PollHistoryState(
            paginationState = paginationState,
            matrixTimelineItems = items.toImmutableList(),
            eventSink = ::handleEvents,
        )
    }
}
