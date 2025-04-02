/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
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
import io.element.android.libraries.matrix.api.timeline.Timeline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class PollHistoryPresenter @Inject constructor(
    private val appCoroutineScope: CoroutineScope,
    private val sendPollResponseAction: SendPollResponseAction,
    private val endPollAction: EndPollAction,
    private val pollHistoryItemFactory: PollHistoryItemsFactory,
    private val room: MatrixRoom,
) : Presenter<PollHistoryState> {
    @Composable
    override fun present(): PollHistoryState {
        val paginationState by produceState(Timeline.PaginationStatus(isPaginating = false, hasMoreToLoad = true)) {
            room.liveTimeline()
                .paginationStatus(Timeline.PaginationDirection.BACKWARDS)
                .collectLatest { value = it }
        }
        var activeFilter by rememberSaveable {
            mutableStateOf(PollHistoryFilter.ONGOING)
        }
        val pollHistoryItems by produceState(PollHistoryItems()) {
            room.liveTimeline()
                .timelineItems
                .collectLatest { items ->
                    value = pollHistoryItemFactory.create(items)
                }
        }
        LaunchedEffect(paginationState, pollHistoryItems.size) {
            if (pollHistoryItems.size == 0 && paginationState.canPaginate) loadMore()
        }
        val isLoading by remember {
            derivedStateOf {
                pollHistoryItems.size == 0 || paginationState.isPaginating
            }
        }
        val coroutineScope = rememberCoroutineScope()
        fun handleEvents(event: PollHistoryEvents) {
            when (event) {
                is PollHistoryEvents.LoadMore -> {
                    coroutineScope.loadMore()
                }
                is PollHistoryEvents.SelectPollAnswer -> appCoroutineScope.launch {
                    sendPollResponseAction.execute(pollStartId = event.pollStartId, answerId = event.answerId)
                }
                is PollHistoryEvents.EndPoll -> appCoroutineScope.launch {
                    endPollAction.execute(pollStartId = event.pollStartId)
                }
                is PollHistoryEvents.SelectFilter -> {
                    activeFilter = event.filter
                }
            }
        }

        return PollHistoryState(
            isLoading = isLoading,
            hasMoreToLoad = paginationState.hasMoreToLoad,
            pollHistoryItems = pollHistoryItems,
            activeFilter = activeFilter,
            eventSink = ::handleEvents,
        )
    }

    private fun CoroutineScope.loadMore() = launch {
        room.liveTimeline().paginate(Timeline.PaginationDirection.BACKWARDS)
    }
}
