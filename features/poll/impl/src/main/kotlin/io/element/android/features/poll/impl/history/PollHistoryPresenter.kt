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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.poll.api.actions.EndPollAction
import io.element.android.features.poll.api.actions.SendPollResponseAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.timeline.MatrixTimeline
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PollHistoryPresenter @AssistedInject constructor(
    @Assisted private val pollHistory: MatrixTimeline,
    private val appCoroutineScope: CoroutineScope,
    private val sendPollResponseAction: SendPollResponseAction,
    private val endPollAction: EndPollAction,
    private val pollHistoryItemFactory: PollHistoryItemsFactory,
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
                pollHistoryItemFactory.create(items)
            }
        }
        val items by timelineItemsFlow.collectAsState(initial = emptyList())
        LaunchedEffect(items.size) {
            if (items.isEmpty()) loadMore()
        }
        val coroutineScope = rememberCoroutineScope()
        fun handleEvents(event: PollHistoryEvents) {
            when (event) {
                is PollHistoryEvents.LoadMore -> {
                    coroutineScope.loadMore()
                }
                is PollHistoryEvents.PollAnswerSelected -> appCoroutineScope.launch {
                    sendPollResponseAction.execute(pollStartId = event.pollStartId, answerId = event.answerId)
                }
                is PollHistoryEvents.PollEndClicked -> appCoroutineScope.launch {
                    endPollAction.execute(pollStartId = event.pollStartId)
                }
                PollHistoryEvents.EditPoll -> Unit
            }
        }
        return PollHistoryState(
            paginationState = paginationState,
            pollItems = items.toImmutableList(),
            eventSink = ::handleEvents,
        )
    }

    private fun CoroutineScope.loadMore() = launch {
        pollHistory.paginateBackwards(20, 3)
    }
}
