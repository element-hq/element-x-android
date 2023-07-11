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

@file:OptIn(ExperimentalAnimationApi::class)

package io.element.android.features.messages.impl.timeline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.impl.R
import io.element.android.features.messages.impl.timeline.components.TimelineItemEventRow
import io.element.android.features.messages.impl.timeline.components.TimelineItemStateEventRow
import io.element.android.features.messages.impl.timeline.components.TimelineItemVirtualRow
import io.element.android.features.messages.impl.timeline.components.group.GroupHeaderView
import io.element.android.features.messages.impl.timeline.components.virtual.TimelineLoadingMoreIndicator
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContentProvider
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStateContent
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemEncryptedHistoryBannerVirtualModel
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.FloatingActionButton
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.theme.ElementTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch

@Composable
fun TimelineView(
    state: TimelineState,
    onUserDataClicked: (UserId) -> Unit,
    onMessageClicked: (TimelineItem.Event) -> Unit,
    onMessageLongClicked: (TimelineItem.Event) -> Unit,
    onTimestampClicked: (TimelineItem.Event) -> Unit,
    onSwipeToReply: (TimelineItem.Event) -> Unit,
    onReactionClicked: (emoji: String, TimelineItem.Event) -> Unit,
    onMoreReactionsClicked: (TimelineItem.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    fun onReachedLoadMore() {
        state.eventSink(TimelineEvents.LoadMore)
    }

    fun onScrollFinishedAt(firstVisibleIndex: Int) {
        state.eventSink(TimelineEvents.OnScrollFinished(firstVisibleIndex))
    }

    val lazyListState = rememberLazyListState()

    fun inReplyToClicked(eventId: EventId) {
        // TODO implement this logic once we have support to 'jump to event X' in sliding sync
    }

    // Send an event to the presenter when the scrolling is finished, with the first visible index at the bottom.
    val firstVisibleIndex by remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }
    val isScrollFinished by remember { derivedStateOf { !lazyListState.isScrollInProgress } }
    LaunchedEffect(firstVisibleIndex, isScrollFinished) {
        if (!isScrollFinished) return@LaunchedEffect
        state.eventSink(TimelineEvents.OnScrollFinished(firstVisibleIndex))
    }

    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            reverseLayout = true,
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            items(
                items = state.timelineItems,
                contentType = { timelineItem -> timelineItem.contentType() },
                key = { timelineItem -> timelineItem.identifier() },
            ) { timelineItem ->
                TimelineItemRow(
                    timelineItem = timelineItem,
                    highlightedItem = state.highlightedEventId?.value,
                    canReply = state.canReply,
                    onClick = onMessageClicked,
                    onLongClick = onMessageLongClicked,
                    onUserDataClick = onUserDataClicked,
                    inReplyToClick = ::inReplyToClicked,
                    onReactionClick = onReactionClicked,
                    onMoreReactionsClick = onMoreReactionsClicked,
                    onTimestampClicked = onTimestampClicked,
                    onSwipeToReply = onSwipeToReply,
                )
            }
            if (state.paginationState.canBackPaginate) {
                // Do not use key parameter to avoid wrong positioning
                item(contentType = "TimelineLoadingMoreIndicator") {
                    TimelineLoadingMoreIndicator()
                    LaunchedEffect(Unit) {
                        onReachedLoadMore()
                    }
                }
            }
        }

        TimelineScrollHelper(
            lazyListState = lazyListState,
            timelineItems = state.timelineItems,
            onScrollFinishedAt = ::onScrollFinishedAt,
        )
    }
}

@Composable
fun TimelineItemRow(
    timelineItem: TimelineItem,
    highlightedItem: String?,
    canReply: Boolean,
    onUserDataClick: (UserId) -> Unit,
    onClick: (TimelineItem.Event) -> Unit,
    onLongClick: (TimelineItem.Event) -> Unit,
    inReplyToClick: (EventId) -> Unit,
    onReactionClick: (key: String, TimelineItem.Event) -> Unit,
    onMoreReactionsClick: (TimelineItem.Event) -> Unit,
    onTimestampClicked: (TimelineItem.Event) -> Unit,
    onSwipeToReply: (TimelineItem.Event) -> Unit,
    modifier: Modifier = Modifier
) {
    when (timelineItem) {
        is TimelineItem.Virtual -> {
            TimelineItemVirtualRow(
                virtual = timelineItem,
                modifier = modifier,
            )
        }
        is TimelineItem.Event -> {
            if (timelineItem.content is TimelineItemStateContent) {
                TimelineItemStateEventRow(
                    event = timelineItem,
                    isHighlighted = highlightedItem == timelineItem.identifier(),
                    onClick = { onClick(timelineItem) },
                    onLongClick = { onLongClick(timelineItem) },
                    modifier = modifier,
                )
            } else {
                TimelineItemEventRow(
                    event = timelineItem,
                    isHighlighted = highlightedItem == timelineItem.identifier(),
                    canReply = canReply,
                    onClick = { onClick(timelineItem) },
                    onLongClick = { onLongClick(timelineItem) },
                    onUserDataClick = onUserDataClick,
                    inReplyToClick = inReplyToClick,
                    onReactionClick = onReactionClick,
                    onMoreReactionsClick = onMoreReactionsClick,
                    onTimestampClicked = onTimestampClicked,
                    onSwipeToReply = { onSwipeToReply(timelineItem) },
                    modifier = modifier,
                )
            }
        }
        is TimelineItem.GroupedEvents -> {
            val isExpanded = rememberSaveable(key = timelineItem.identifier()) { mutableStateOf(false) }

            fun onExpandGroupClick() {
                isExpanded.value = !isExpanded.value
            }

            Column(modifier = modifier.animateContentSize()) {
                GroupHeaderView(
                    text = pluralStringResource(
                        id = R.plurals.room_timeline_state_changes,
                        count = timelineItem.events.size,
                        timelineItem.events.size
                    ),
                    isExpanded = isExpanded.value,
                    isHighlighted = !isExpanded.value && timelineItem.events.any { it.identifier() == highlightedItem },
                    onClick = ::onExpandGroupClick,
                )
                if (isExpanded.value) {
                    Column {
                        timelineItem.events.forEach { subGroupEvent ->
                            TimelineItemRow(
                                timelineItem = subGroupEvent,
                                highlightedItem = highlightedItem,
                                canReply = false,
                                onClick = onClick,
                                onLongClick = onLongClick,
                                inReplyToClick = inReplyToClick,
                                onUserDataClick = onUserDataClick,
                                onTimestampClicked = onTimestampClicked,
                                onReactionClick = onReactionClick,
                                onMoreReactionsClick = onMoreReactionsClick,
                                onSwipeToReply = {},
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun BoxScope.TimelineScrollHelper(
    lazyListState: LazyListState,
    timelineItems: ImmutableList<TimelineItem>,
    onScrollFinishedAt: (Int) -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val firstVisibleItemIndex by remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }
    val isScrollFinished by remember { derivedStateOf { !lazyListState.isScrollInProgress } }
    val shouldAutoScrollToBottom by remember { derivedStateOf { lazyListState.firstVisibleItemIndex < 2 } }
    val showScrollToBottomButton by remember { derivedStateOf { lazyListState.firstVisibleItemIndex > 0 } }

    LaunchedEffect(timelineItems, firstVisibleItemIndex) {
        if (!isScrollFinished) return@LaunchedEffect

        // Auto-scroll when new timeline items appear
        if (shouldAutoScrollToBottom) {
            coroutineScope.launch {
                lazyListState.animateScrollToItem(0)
            }
        }
    }
    LaunchedEffect(isScrollFinished) {
        if (!isScrollFinished) return@LaunchedEffect

        // Notify the parent composable about the first visible item index when scrolling finishes
        onScrollFinishedAt(firstVisibleItemIndex)
    }

    // Jump to bottom button (display also in previews)
    AnimatedVisibility(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 24.dp, bottom = 12.dp),
        visible = showScrollToBottomButton || LocalInspectionMode.current,
        enter = scaleIn(),
        exit = scaleOut(),
    ) {
        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    if (firstVisibleItemIndex > 10) {
                        lazyListState.scrollToItem(0)
                    } else {
                        lazyListState.animateScrollToItem(0)
                    }
                }
            },
            elevation = FloatingActionButtonDefaults.elevation(4.dp, 4.dp, 4.dp, 4.dp),
            shape = CircleShape,
            modifier = Modifier
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape,
                    ambientColor = ElementTheme.materialColors.primary,
                    spotColor = ElementTheme.materialColors.primary,
                )
                .size(36.dp),
            containerColor = ElementTheme.colors.bgSubtleSecondary,
            contentColor = ElementTheme.colors.iconSecondary
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = Icons.Filled.ArrowDownward,
                contentDescription = "",
            )
        }
    }
}

@Preview
@Composable
fun TimelineViewLightPreview(
    @PreviewParameter(TimelineItemEventContentProvider::class) content: TimelineItemEventContent
) = ElementPreviewLight { ContentToPreview(content) }

@Preview
@Composable
fun TimelineViewDarkPreview(
    @PreviewParameter(TimelineItemEventContentProvider::class) content: TimelineItemEventContent
) = ElementPreviewDark { ContentToPreview(content) }

@Composable
private fun ContentToPreview(content: TimelineItemEventContent) {
    val timelineItems = buildList {
        addAll(aTimelineItemList(content))
        add(TimelineItem.Virtual("banner", TimelineItemEncryptedHistoryBannerVirtualModel))
    }.toPersistentList()
    TimelineView(
        state = aTimelineState(timelineItems),
        onMessageClicked = {},
        onTimestampClicked = {},
        onUserDataClicked = {},
        onMessageLongClicked = {},
        onReactionClicked = { _, _ -> },
        onMoreReactionsClicked = {},
        onSwipeToReply = {},
    )
}
