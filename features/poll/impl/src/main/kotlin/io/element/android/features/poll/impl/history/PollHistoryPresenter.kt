/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
import dev.zacsweers.metro.Inject
import io.element.android.features.poll.api.actions.EndPollAction
import io.element.android.features.poll.api.actions.SendPollResponseAction
import io.element.android.features.poll.impl.history.model.PollHistoryFilter
import io.element.android.features.poll.impl.history.model.PollHistoryItems
import io.element.android.features.poll.impl.history.model.PollHistoryItemsFactory
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.timeline.Timeline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Inject
class PollHistoryPresenter(
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
    private val sendPollResponseAction: SendPollResponseAction,
    private val endPollAction: EndPollAction,
    private val pollHistoryItemFactory: PollHistoryItemsFactory,
    private val room: JoinedRoom,
) : Presenter<PollHistoryState> {
    @Composable
    override fun present(): PollHistoryState {
        val timeline = room.liveTimeline
        val paginationState by timeline.backwardPaginationStatus.collectAsState()
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
            if (pollHistoryItems.size == 0 && paginationState.canPaginate) loadMore(timeline)
        }
        val isLoading by remember {
            derivedStateOf {
                pollHistoryItems.size == 0 || paginationState.isPaginating
            }
        }
        val coroutineScope = rememberCoroutineScope()
        fun handleEvent(event: PollHistoryEvents) {
            when (event) {
                is PollHistoryEvents.LoadMore -> {
                    coroutineScope.loadMore(timeline)
                }
                is PollHistoryEvents.SelectPollAnswer -> sessionCoroutineScope.launch {
                    sendPollResponseAction.execute(
                        timeline = timeline,
                        pollStartId = event.pollStartId,
                        answerId = event.answerId
                    )
                }
                is PollHistoryEvents.EndPoll -> sessionCoroutineScope.launch {
                    endPollAction.execute(timeline = timeline, pollStartId = event.pollStartId)
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
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.loadMore(pollHistory: Timeline) = launch {
        pollHistory.paginate(Timeline.PaginationDirection.BACKWARDS)
    }
}
