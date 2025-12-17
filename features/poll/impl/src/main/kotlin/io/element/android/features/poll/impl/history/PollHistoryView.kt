/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.poll.api.pollcontent.PollContentView
import io.element.android.features.poll.impl.R
import io.element.android.features.poll.impl.history.model.PollHistoryFilter
import io.element.android.features.poll.impl.history.model.PollHistoryItem
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.SegmentedButton
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PollHistoryView(
    state: PollHistoryState,
    onEditPoll: (EventId) -> Unit,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    fun onLoadMore() {
        state.eventSink(PollHistoryEvents.LoadMore)
    }

    fun onSelectAnswer(pollStartId: EventId, answerId: String) {
        state.eventSink(PollHistoryEvents.SelectPollAnswer(pollStartId, answerId))
    }

    fun onEndPoll(pollStartId: EventId) {
        state.eventSink(PollHistoryEvents.EndPoll(pollStartId))
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                titleStr = stringResource(R.string.screen_polls_history_title),
                navigationIcon = {
                    BackButton(onClick = goBack)
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {
            val pagerState = rememberPagerState(state.activeFilter.ordinal, 0f) {
                PollHistoryFilter.entries.size
            }
            LaunchedEffect(state.activeFilter) {
                pagerState.scrollToPage(state.activeFilter.ordinal)
            }
            PollHistoryFilterButtons(
                activeFilter = state.activeFilter,
                onSelectFilter = { state.eventSink(PollHistoryEvents.SelectFilter(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val filter = PollHistoryFilter.entries[page]
                val pollHistoryItems = state.pollHistoryForFilter(filter)
                PollHistoryList(
                    filter = filter,
                    pollHistoryItems = pollHistoryItems,
                    hasMoreToLoad = state.hasMoreToLoad,
                    isLoading = state.isLoading,
                    onSelectAnswer = ::onSelectAnswer,
                    onEditPoll = onEditPoll,
                    onEndPoll = ::onEndPoll,
                    onLoadMore = ::onLoadMore,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PollHistoryFilterButtons(
    activeFilter: PollHistoryFilter,
    onSelectFilter: (PollHistoryFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        PollHistoryFilter.entries.forEach { filter ->
            SegmentedButton(
                index = filter.ordinal,
                count = PollHistoryFilter.entries.size,
                selected = activeFilter == filter,
                onClick = { onSelectFilter(filter) },
                text = stringResource(filter.stringResource),
            )
        }
    }
}

@Composable
private fun PollHistoryList(
    filter: PollHistoryFilter,
    pollHistoryItems: ImmutableList<PollHistoryItem>,
    hasMoreToLoad: Boolean,
    isLoading: Boolean,
    onSelectAnswer: (pollStartId: EventId, answerId: String) -> Unit,
    onEditPoll: (pollStartId: EventId) -> Unit,
    onEndPoll: (pollStartId: EventId) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    LazyColumn(
        state = lazyListState,
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(pollHistoryItems) { pollHistoryItem ->
            PollHistoryItemRow(
                pollHistoryItem = pollHistoryItem,
                onSelectAnswer = onSelectAnswer,
                onEditPoll = onEditPoll,
                onEndPoll = onEndPoll,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
            )
        }
        if (pollHistoryItems.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillParentMaxSize()
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val emptyStringResource = if (filter == PollHistoryFilter.PAST) {
                        stringResource(R.string.screen_polls_history_empty_past)
                    } else {
                        stringResource(R.string.screen_polls_history_empty_ongoing)
                    }
                    Text(
                        text = emptyStringResource,
                        style = ElementTheme.typography.fontBodyLgRegular,
                        color = ElementTheme.colors.textSecondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp, horizontal = 16.dp),
                        textAlign = TextAlign.Center,
                    )

                    if (hasMoreToLoad) {
                        LoadMoreButton(isLoading = isLoading, onClick = onLoadMore)
                    }
                }
            }
        } else if (hasMoreToLoad) {
            item {
                LoadMoreButton(isLoading = isLoading, onClick = onLoadMore)
            }
        }
    }
}

@Composable
private fun LoadMoreButton(isLoading: Boolean, onClick: () -> Unit) {
    Button(
        text = stringResource(CommonStrings.action_load_more),
        showProgress = isLoading,
        onClick = onClick,
        modifier = Modifier.padding(vertical = 24.dp),
    )
}

@Composable
private fun PollHistoryItemRow(
    pollHistoryItem: PollHistoryItem,
    onSelectAnswer: (pollStartId: EventId, answerId: String) -> Unit,
    onEditPoll: (pollStartId: EventId) -> Unit,
    onEndPoll: (pollStartId: EventId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.semantics(mergeDescendants = true) {
            // Allow the answers to be traversed by Talkback
            isTraversalGroup = true
        },
        border = BorderStroke(1.dp, ElementTheme.colors.borderInteractiveSecondary),
        shape = RoundedCornerShape(size = 12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = pollHistoryItem.formattedDate,
                color = ElementTheme.colors.textSecondary,
                style = ElementTheme.typography.fontBodySmRegular,
            )
            Spacer(modifier = Modifier.height(4.dp))
            PollContentView(
                state = pollHistoryItem.state,
                onSelectAnswer = onSelectAnswer,
                onEditPoll = onEditPoll,
                onEndPoll = onEndPoll,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun PollHistoryViewPreview(
    @PreviewParameter(PollHistoryStateProvider::class) state: PollHistoryState
) = ElementPreview {
    PollHistoryView(
        state = state,
        onEditPoll = {},
        goBack = {},
    )
}
