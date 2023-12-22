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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.element.android.features.poll.api.actions.EndPollAction
import io.element.android.features.poll.api.actions.SendPollResponseAction
import io.element.android.features.poll.impl.history.model.PollHistoryFilter
import io.element.android.features.poll.impl.history.model.PollHistoryItems
import io.element.android.features.poll.impl.history.model.PollHistoryItemsFactory
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.timeline.MatrixTimeline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class PollHistoryPresenter @Inject constructor(
    private val room: MatrixRoom,
    private val appCoroutineScope: CoroutineScope,
    private val sendPollResponseAction: SendPollResponseAction,
    private val endPollAction: EndPollAction,
    private val pollHistoryItemFactory: PollHistoryItemsFactory,
) : Presenter<PollHistoryState> {

    @Composable
    override fun present(): PollHistoryState {
        // TODO use room.rememberPollHistory() when working properly?
        val timeline = room.timeline
        val paginationState by timeline.paginationState.collectAsState()
        val pollHistoryItemsFlow = remember {
            timeline.timelineItems.map { items ->
                pollHistoryItemFactory.create(items)
            }
        }
        var activeFilter by rememberSaveable {
            mutableStateOf(PollHistoryFilter.ONGOING)
        }
        val pollHistoryItems by pollHistoryItemsFlow.collectAsState(initial = PollHistoryItems())
        LaunchedEffect(paginationState, pollHistoryItems.size) {
            if (pollHistoryItems.size == 0 && paginationState.canBackPaginate) loadMore(timeline)
        }
        val isLoading by remember {
            derivedStateOf {
                pollHistoryItems.size == 0 || paginationState.isBackPaginating
            }
        }
        val coroutineScope = rememberCoroutineScope()
        fun handleEvents(event: PollHistoryEvents) {
            when (event) {
                is PollHistoryEvents.LoadMore -> {
                    coroutineScope.loadMore(timeline)
                }
                is PollHistoryEvents.PollAnswerSelected -> appCoroutineScope.launch {
                    sendPollResponseAction.execute(pollStartId = event.pollStartId, answerId = event.answerId)
                }
                is PollHistoryEvents.PollEndClicked -> appCoroutineScope.launch {
                    endPollAction.execute(pollStartId = event.pollStartId)
                }
                is PollHistoryEvents.OnFilterSelected -> {
                    activeFilter = event.filter
                }
            }
        }


        return PollHistoryState(
            isLoading = isLoading,
            hasMoreToLoad = paginationState.hasMoreToLoadBackwards,
            pollHistoryItems = pollHistoryItems,
            activeFilter = activeFilter,
            eventSink = ::handleEvents,
        )
    }

    private fun CoroutineScope.loadMore(pollHistory: MatrixTimeline) = launch {
        pollHistory.paginateBackwards(200)
    }
}
