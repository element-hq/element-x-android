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

package io.element.android.features.messages.timeline

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.element.android.features.messages.timeline.components.MessageEventBubble
import io.element.android.features.messages.timeline.components.TimelineItemEncryptedView
import io.element.android.features.messages.timeline.components.TimelineItemImageView
import io.element.android.features.messages.timeline.components.TimelineItemReactionsView
import io.element.android.features.messages.timeline.components.TimelineItemRedactedView
import io.element.android.features.messages.timeline.components.TimelineItemTextView
import io.element.android.features.messages.timeline.components.TimelineItemUnknownView
import io.element.android.features.messages.timeline.model.AggregatedReaction
import io.element.android.features.messages.timeline.model.MessagesItemGroupPosition
import io.element.android.features.messages.timeline.model.TimelineItem
import io.element.android.features.messages.timeline.model.TimelineItemReactions
import io.element.android.features.messages.timeline.model.content.MessagesTimelineItemContentProvider
import io.element.android.features.messages.timeline.model.content.TimelineItemContent
import io.element.android.features.messages.timeline.model.content.TimelineItemEncryptedContent
import io.element.android.features.messages.timeline.model.content.TimelineItemImageContent
import io.element.android.features.messages.timeline.model.content.TimelineItemRedactedContent
import io.element.android.features.messages.timeline.model.content.TimelineItemTextBasedContent
import io.element.android.features.messages.timeline.model.content.TimelineItemTextContent
import io.element.android.features.messages.timeline.model.content.TimelineItemUnknownContent
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.FloatingActionButton
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.core.EventId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun TimelineView(
    state: TimelineState,
    modifier: Modifier = Modifier,
    onMessageClicked: (TimelineItem.MessageEvent) -> Unit = {},
    onMessageLongClicked: (TimelineItem.MessageEvent) -> Unit = {},
) {
    val lazyListState = rememberLazyListState()
    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Bottom,
            reverseLayout = true
        ) {
            items(
                items = state.timelineItems,
                contentType = { timelineItem -> timelineItem.contentType() },
                key = { timelineItem -> timelineItem.key() },
            ) { timelineItem ->
                TimelineItemRow(
                    timelineItem = timelineItem,
                    isHighlighted = timelineItem.key() == state.highlightedEventId?.value,
                    onClick = onMessageClicked,
                    onLongClick = onMessageLongClicked
                )
            }
            if (state.hasMoreToLoad) {
                item {
                    TimelineLoadingMoreIndicator()
                }
            }
        }

        fun onReachedLoadMore() {
            state.eventSink(TimelineEvents.LoadMore)
        }

        TimelineScrollHelper(
            lazyListState = lazyListState,
            timelineItems = state.timelineItems,
            onLoadMore = ::onReachedLoadMore
        )
    }
}

private fun TimelineItem.key(): String {
    return when (this) {
        is TimelineItem.MessageEvent -> id.value
        is TimelineItem.Virtual -> id
    }
}

private fun TimelineItem.contentType(): Int {
    return when (this) {
        is TimelineItem.MessageEvent -> 0
        is TimelineItem.Virtual -> 1
    }
}

@Composable
fun TimelineItemRow(
    timelineItem: TimelineItem,
    isHighlighted: Boolean,
    onClick: (TimelineItem.MessageEvent) -> Unit,
    onLongClick: (TimelineItem.MessageEvent) -> Unit,
) {
    when (timelineItem) {
        is TimelineItem.Virtual -> return
        is TimelineItem.MessageEvent -> MessageEventRow(
            messageEvent = timelineItem,
            isHighlighted = isHighlighted,
            onClick = { onClick(timelineItem) },
            onLongClick = { onLongClick(timelineItem) }
        )
    }
}

@Composable
fun MessageEventRow(
    messageEvent: TimelineItem.MessageEvent,
    isHighlighted: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val (parentAlignment, contentAlignment) = if (messageEvent.isMine) {
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
            if (!messageEvent.isMine) {
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(horizontalAlignment = contentAlignment) {
                if (messageEvent.showSenderInformation) {
                    MessageSenderInformation(
                        messageEvent.safeSenderName,
                        messageEvent.senderAvatar,
                        Modifier.zIndex(1f)
                    )
                }
                MessageEventBubble(
                    groupPosition = messageEvent.groupPosition,
                    isMine = messageEvent.isMine,
                    interactionSource = interactionSource,
                    isHighlighted = isHighlighted,
                    onClick = onClick,
                    onLongClick = onLongClick,
                    modifier = Modifier
                        .zIndex(-1f)
                        .widthIn(max = 320.dp)
                ) {
                    val contentModifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    when (messageEvent.content) {
                        is TimelineItemEncryptedContent -> TimelineItemEncryptedView(
                            content = messageEvent.content,
                            modifier = contentModifier
                        )
                        is TimelineItemRedactedContent -> TimelineItemRedactedView(
                            content = messageEvent.content,
                            modifier = contentModifier
                        )
                        is TimelineItemTextBasedContent -> TimelineItemTextView(
                            content = messageEvent.content,
                            interactionSource = interactionSource,
                            modifier = contentModifier,
                            onTextClicked = onClick,
                            onTextLongClicked = onLongClick
                        )
                        is TimelineItemUnknownContent -> TimelineItemUnknownView(
                            content = messageEvent.content,
                            modifier = contentModifier
                        )
                        is TimelineItemImageContent -> TimelineItemImageView(
                            content = messageEvent.content,
                            modifier = contentModifier
                        )
                    }
                }
                TimelineItemReactionsView(
                    reactionsState = messageEvent.reactionsState,
                    modifier = Modifier
                        .zIndex(1f)
                        .offset(x = if (messageEvent.isMine) 0.dp else 20.dp, y = -(16.dp))
                )
            }
            if (messageEvent.isMine) {
                Spacer(modifier = Modifier.width(16.dp))
            }
        }
    }
    if (messageEvent.groupPosition.isNew()) {
        Spacer(modifier = modifier.height(8.dp))
    } else {
        Spacer(modifier = modifier.height(2.dp))
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

@Composable
internal fun TimelineLoadingMoreIndicator() {
    Box(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview
@Composable
fun LoginRootScreenLightPreview(
    @PreviewParameter(MessagesTimelineItemContentProvider::class) content: TimelineItemContent
) = ElementPreviewLight { ContentToPreview(content) }

@Preview
@Composable
fun LoginRootScreenDarkPreview(
    @PreviewParameter(MessagesTimelineItemContentProvider::class) content: TimelineItemContent
) = ElementPreviewDark { ContentToPreview(content) }

@Composable
private fun ContentToPreview(content: TimelineItemContent) {
    val timelineItems = createTimelineItems(content)
    TimelineView(
        state = aTimelineState().copy(
            timelineItems = timelineItems,
            hasMoreToLoad = true,
        )
    )
}

internal fun createTimelineItems(content: TimelineItemContent): ImmutableList<TimelineItem> {
    return persistentListOf(
        // 3 items (First Middle Last) with isMine = false
        createMessageEvent(
            isMine = false,
            content = content,
            groupPosition = MessagesItemGroupPosition.Last
        ),
        createMessageEvent(
            isMine = false,
            content = content,
            groupPosition = MessagesItemGroupPosition.Middle
        ),
        createMessageEvent(
            isMine = false,
            content = content,
            groupPosition = MessagesItemGroupPosition.First
        ),
        // 3 items (First Middle Last) with isMine = true
        createMessageEvent(
            isMine = true,
            content = content,
            groupPosition = MessagesItemGroupPosition.Last
        ),
        createMessageEvent(
            isMine = true,
            content = content,
            groupPosition = MessagesItemGroupPosition.Middle
        ),
        createMessageEvent(
            isMine = true,
            content = content,
            groupPosition = MessagesItemGroupPosition.First
        ),
    )
}

internal fun createMessageEvent(
    isMine: Boolean = false,
    content: TimelineItemContent = createTimelineItemContent(),
    groupPosition: MessagesItemGroupPosition = MessagesItemGroupPosition.First
): TimelineItem.MessageEvent {
    return TimelineItem.MessageEvent(
        id = EventId(Math.random().toString()),
        senderId = "senderId",
        senderAvatar = AvatarData("sender"),
        content = content,
        reactionsState = TimelineItemReactions(
            persistentListOf(
                AggregatedReaction("👍", "1")
            )
        ),
        isMine = isMine,
        senderDisplayName = "sender",
        groupPosition = groupPosition,
    )
}

internal fun createTimelineItemContent(): TimelineItemContent {
    return TimelineItemTextContent(
        body = "Text",
        htmlDocument = null
    )
}
