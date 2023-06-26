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

package io.element.android.features.messages.impl.timeline

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.impl.R
import io.element.android.features.messages.impl.timeline.components.TimelineItemEventRow
import io.element.android.features.messages.impl.timeline.components.TimelineItemStateEventRow
import io.element.android.features.messages.impl.timeline.components.TimelineItemVirtualRow
import io.element.android.features.messages.impl.timeline.components.group.GroupHeaderView
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContentProvider
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStateContent
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.FloatingActionButton
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun TimelineView(
    state: TimelineState,
    onUserDataClicked: (UserId) -> Unit,
    onMessageClicked: (TimelineItem.Event) -> Unit,
    onMessageLongClicked: (TimelineItem.Event) -> Unit,
    onTimestampClicked: (TimelineItem.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    fun onReachedLoadMore() {
        state.eventSink(TimelineEvents.LoadMore)
    }

    val lazyListState = rememberLazyListState()

    fun inReplyToClicked(eventId: EventId) {
        // TODO implement this logic once we have support to 'jump to event X' in sliding sync
    }

    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            reverseLayout = true,
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            itemsIndexed(
                items = state.timelineItems,
                contentType = { _, timelineItem -> timelineItem.contentType() },
                key = { _, timelineItem -> timelineItem.identifier() },
            ) { index, timelineItem ->
                TimelineItemRow(
                    timelineItem = timelineItem,
                    highlightedItem = state.highlightedEventId?.value,
                    onClick = onMessageClicked,
                    onLongClick = onMessageLongClicked,
                    onUserDataClick = onUserDataClicked,
                    inReplyToClick = ::inReplyToClicked,
                    onTimestampClicked = onTimestampClicked,
                )
                if (index == state.timelineItems.lastIndex) {
                    onReachedLoadMore()
                }
            }
        }

        TimelineScrollHelper(
            lazyListState = lazyListState,
            timelineItems = state.timelineItems,
            onLoadMore = ::onReachedLoadMore
        )
    }
}

@Composable
fun TimelineItemRow(
    timelineItem: TimelineItem,
    highlightedItem: String?,
    onUserDataClick: (UserId) -> Unit,
    onClick: (TimelineItem.Event) -> Unit,
    onLongClick: (TimelineItem.Event) -> Unit,
    inReplyToClick: (EventId) -> Unit,
    onTimestampClicked: (TimelineItem.Event) -> Unit,
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
            fun onClick() {
                onClick(timelineItem)
            }

            fun onLongClick() {
                onLongClick(timelineItem)
            }

            if (timelineItem.content is TimelineItemStateContent) {
                TimelineItemStateEventRow(
                    event = timelineItem,
                    isHighlighted = highlightedItem == timelineItem.identifier(),
                    onClick = ::onClick,
                    onLongClick = ::onLongClick,
                    modifier = modifier,
                )
            } else {
                TimelineItemEventRow(
                    event = timelineItem,
                    isHighlighted = highlightedItem == timelineItem.identifier(),
                    onClick = ::onClick,
                    onLongClick = ::onLongClick,
                    onUserDataClick = onUserDataClick,
                    inReplyToClick = inReplyToClick,
                    onTimestampClicked = onTimestampClicked,
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
                                onClick = onClick,
                                onLongClick = onLongClick,
                                inReplyToClick = inReplyToClick,
                                onUserDataClick = onUserDataClick,
                                onTimestampClicked = onTimestampClicked,
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
    onLoadMore: () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val firstVisibleItemIndex by remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }

    // Auto-scroll when new timeline items appear
    LaunchedEffect(timelineItems, firstVisibleItemIndex) {
        if (!lazyListState.isScrollInProgress &&
            firstVisibleItemIndex < 2
        ) coroutineScope.launch {
            lazyListState.animateScrollToItem(0)
        }
    }

    // Handle load more preloading
    val loadMore by remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
            lastVisibleItemIndex > (totalItemsNumber - 30)
        }
    }

    LaunchedEffect(loadMore) {
        snapshotFlow { loadMore }
            .distinctUntilChanged()
            .collect {
                onLoadMore()
            }
    }

    // Jump to bottom button
    if (firstVisibleItemIndex > 2) {
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
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(40.dp),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Icon(Icons.Default.ArrowDownward, "")
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
    val timelineItems = aTimelineItemList(content)
    TimelineView(
        state = aTimelineState(timelineItems),
        onMessageClicked = {},
        onTimestampClicked = {},
        onUserDataClicked = {},
        onMessageLongClicked = {},
    )
}
