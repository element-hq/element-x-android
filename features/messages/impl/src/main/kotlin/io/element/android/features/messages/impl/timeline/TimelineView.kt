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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.element.android.features.messages.impl.R
import io.element.android.features.messages.impl.timeline.components.MessageEventBubble
import io.element.android.features.messages.impl.timeline.components.MessageStateEventContainer
import io.element.android.features.messages.impl.timeline.components.TimelineItemReactionsView
import io.element.android.features.messages.impl.timeline.components.event.TimelineItemEventContentView
import io.element.android.features.messages.impl.timeline.components.group.GroupHeaderView
import io.element.android.features.messages.impl.timeline.components.virtual.TimelineItemDaySeparatorView
import io.element.android.features.messages.impl.timeline.components.virtual.TimelineLoadingMoreIndicator
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.bubble.BubbleState
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContentProvider
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStateContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemDaySeparatorModel
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemLoadingModel
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.designsystem.ElementTextStyles
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.ElementTheme
import io.element.android.libraries.designsystem.theme.components.FloatingActionButton
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.timeline.item.event.EventSendState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import io.element.android.libraries.ui.strings.R as StringR

@Composable
fun TimelineView(
    state: TimelineState,
    modifier: Modifier = Modifier,
    onMessageClicked: (TimelineItem.Event) -> Unit = {},
    onMessageLongClicked: (TimelineItem.Event) -> Unit = {},
    onExpandGroupClick: (TimelineItem.GroupedEvents) -> Unit = {},
) {

    fun onReachedLoadMore() {
        state.eventSink(TimelineEvents.LoadMore)
    }

    val lazyListState = rememberLazyListState()
    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            reverseLayout = true
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
                    onExpandGroupClick = onExpandGroupClick,
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
    onClick: (TimelineItem.Event) -> Unit,
    onLongClick: (TimelineItem.Event) -> Unit,
    onExpandGroupClick: (TimelineItem.GroupedEvents) -> Unit,
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
                    modifier = modifier,
                )
            }
        }
        is TimelineItem.GroupedEvents -> {
            fun onExpandGroupClick() {
                onExpandGroupClick(timelineItem)
            }

            Column(modifier = modifier.animateContentSize()) {
                GroupHeaderView(
                    text = pluralStringResource(
                        id = R.plurals.room_timeline_state_changes,
                        count = timelineItem.events.size,
                        timelineItem.events.size
                    ),
                    isExpanded = timelineItem.expanded,
                    isHighlighted = !timelineItem.expanded && timelineItem.events.any { it.identifier() == highlightedItem },
                    onClick = ::onExpandGroupClick,
                )
                if (timelineItem.expanded) {
                    Column {
                        timelineItem.events.forEach { subGroupEvent ->
                            TimelineItemRow(
                                timelineItem = subGroupEvent,
                                highlightedItem = highlightedItem,
                                onClick = onClick,
                                onLongClick = onLongClick,
                                onExpandGroupClick = {}
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineItemVirtualRow(
    virtual: TimelineItem.Virtual,
    modifier: Modifier = Modifier
) {
    when (virtual.model) {
        is TimelineItemLoadingModel -> TimelineLoadingMoreIndicator(modifier)
        is TimelineItemDaySeparatorModel -> TimelineItemDaySeparatorView(virtual.model, modifier)
        else -> return
    }
}

@Composable
fun TimelineItemEventRow(
    event: TimelineItem.Event,
    isHighlighted: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val (parentAlignment, contentAlignment) = if (event.isMine) {
        Pair(Alignment.CenterEnd, Alignment.End)
    } else {
        Pair(Alignment.CenterStart, Alignment.Start)
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = parentAlignment
    ) {
        Row {
            if (!event.isMine) {
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(horizontalAlignment = contentAlignment) {
                if (event.showSenderInformation) {
                    MessageSenderInformation(
                        event.safeSenderName,
                        event.senderAvatar,
                        Modifier.zIndex(1f)
                    )
                }
                MessageEventBubble(
                    state = BubbleState(
                        groupPosition = event.groupPosition,
                        isMine = event.isMine,
                        isHighlighted = isHighlighted,
                    ),
                    interactionSource = interactionSource,
                    onClick = onClick,
                    onLongClick = onLongClick,
                    modifier = Modifier
                        .zIndex(-1f)
                        .widthIn(max = 320.dp)
                ) {
                    Column {
                        val contentModifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 6.dp)
                        TimelineItemEventContentView(event.content, interactionSource, onClick, onLongClick, contentModifier)
                        TimestampView(
                            formattedTime = event.sentTime,
                            hasMessageSendingFailed = event.sendState is EventSendState.SendingFailed,
                            isMessageEdited = (event.content as? TimelineItemTextBasedContent)?.isEdited.orFalse(),
                            onClick = {
                                // TODO trigger either resending the message or opening the message edition history. This will be implemented later
                            },
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .align(Alignment.End),
                        )
                    }
                }
                TimelineItemReactionsView(
                    reactionsState = event.reactionsState,
                    modifier = Modifier
                        .zIndex(1f)
                        .offset(x = if (event.isMine) 0.dp else 20.dp, y = -(16.dp))
                )
            }
            if (event.isMine) {
                Spacer(modifier = Modifier.width(16.dp))
            }
        }
    }
    if (event.groupPosition.isNew()) {
        Spacer(modifier = modifier.height(8.dp))
    } else {
        Spacer(modifier = modifier.height(2.dp))
    }
}

@Composable
fun TimelineItemStateEventRow(
    event: TimelineItem.Event,
    isHighlighted: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        MessageStateEventContainer(
            isHighlighted = isHighlighted,
            interactionSource = interactionSource,
            onClick = onClick,
            onLongClick = onLongClick,
            modifier = Modifier
                .zIndex(-1f)
                .widthIn(max = 320.dp)
        ) {
            val contentModifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            TimelineItemEventContentView(
                content = event.content,
                interactionSource = interactionSource,
                onClick = onClick,
                onLongClick = onLongClick,
                modifier = contentModifier
            )
        }
    }
}

@Composable
private fun TimestampView(
    formattedTime: String,
    isMessageEdited: Boolean,
    hasMessageSendingFailed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tint = if (hasMessageSendingFailed) ElementTheme.colors.textActionCritical else null
    Row(modifier = modifier.clickable(onClick = onClick)){
        if (isMessageEdited) {
            Text(
                stringResource(StringR.string.common_edited_suffix),
                style = ElementTextStyles.Regular.caption2,
                color = tint ?: MaterialTheme.colorScheme.secondary,
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            formattedTime,
            style = ElementTextStyles.Regular.caption1,
            color = tint ?: MaterialTheme.colorScheme.secondary,
        )
        if (hasMessageSendingFailed && tint != null) {
            Spacer(modifier = Modifier.width(2.dp))
            Icon(imageVector = Icons.Default.Error, contentDescription = "Error sending message", tint = tint, modifier = Modifier.size(15.dp, 18.dp))
        }
    }
}

@Composable
private fun MessageSenderInformation(
    sender: String,
    senderAvatar: AvatarData?,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        if (senderAvatar != null) {
            Avatar(senderAvatar)
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = sender,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .alignBy(LastBaseline)
        )
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
        state = aTimelineState().copy(
            timelineItems = timelineItems,
        )
    )
}
