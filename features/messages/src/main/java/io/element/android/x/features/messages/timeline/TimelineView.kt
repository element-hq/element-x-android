package io.element.android.x.features.messages.timeline

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import io.element.android.x.architecture.Async
import io.element.android.x.core.compose.PairCombinedPreviewParameter
import io.element.android.x.designsystem.components.avatar.Avatar
import io.element.android.x.designsystem.components.avatar.AvatarData
import io.element.android.x.features.messages.model.AggregatedReaction
import io.element.android.x.features.messages.model.MessagesItemGroupPosition
import io.element.android.x.features.messages.model.MessagesItemGroupPositionProvider
import io.element.android.x.features.messages.model.MessagesItemReactionState
import io.element.android.x.features.messages.model.MessagesTimelineItemState
import io.element.android.x.features.messages.model.content.MessagesTimelineItemContent
import io.element.android.x.features.messages.model.content.MessagesTimelineItemContentProvider
import io.element.android.x.features.messages.model.content.MessagesTimelineItemEncryptedContent
import io.element.android.x.features.messages.model.content.MessagesTimelineItemImageContent
import io.element.android.x.features.messages.model.content.MessagesTimelineItemRedactedContent
import io.element.android.x.features.messages.model.content.MessagesTimelineItemTextBasedContent
import io.element.android.x.features.messages.model.content.MessagesTimelineItemUnknownContent
import io.element.android.x.features.messages.timeline.components.MessageEventBubble
import io.element.android.x.features.messages.timeline.components.MessagesReactionsView
import io.element.android.x.features.messages.timeline.components.MessagesTimelineItemEncryptedView
import io.element.android.x.features.messages.timeline.components.MessagesTimelineItemImageView
import io.element.android.x.features.messages.timeline.components.MessagesTimelineItemRedactedView
import io.element.android.x.features.messages.timeline.components.MessagesTimelineItemTextView
import io.element.android.x.features.messages.timeline.components.MessagesTimelineItemUnknownView
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun TimelineView(
    state: TimelineState,
    modifier: Modifier = Modifier,
    onMessageClicked: (MessagesTimelineItemState.MessageEvent) -> Unit = {},
    onMessageLongClicked: (MessagesTimelineItemState.MessageEvent) -> Unit = {},
) {
    val lazyListState = rememberLazyListState()
    val timelineItems = state.timelineItems.dataOrNull().orEmpty().toImmutableList()

    Box(modifier = modifier.fillMaxWidth()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Bottom,
            reverseLayout = true
        ) {
            items(
                items = timelineItems,
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
            timelineItems = timelineItems,
            onLoadMore = ::onReachedLoadMore
        )
    }
}

private fun MessagesTimelineItemState.key(): String {
    return when (this) {
        is MessagesTimelineItemState.MessageEvent -> id
        is MessagesTimelineItemState.Virtual -> id
    }
}

private fun MessagesTimelineItemState.contentType(): Int {
    return when (this) {
        is MessagesTimelineItemState.MessageEvent -> 0
        is MessagesTimelineItemState.Virtual -> 1
    }
}

@Composable
fun TimelineItemRow(
    timelineItem: MessagesTimelineItemState,
    isHighlighted: Boolean,
    onClick: (MessagesTimelineItemState.MessageEvent) -> Unit,
    onLongClick: (MessagesTimelineItemState.MessageEvent) -> Unit,
) {
    when (timelineItem) {
        is MessagesTimelineItemState.Virtual -> return
        is MessagesTimelineItemState.MessageEvent -> MessageEventRow(
            messageEvent = timelineItem,
            isHighlighted = isHighlighted,
            onClick = { onClick(timelineItem) },
            onLongClick = { onLongClick(timelineItem) }
        )
    }
}

@Composable
fun MessageEventRow(
    messageEvent: MessagesTimelineItemState.MessageEvent,
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
                        is MessagesTimelineItemEncryptedContent -> MessagesTimelineItemEncryptedView(
                            content = messageEvent.content,
                            modifier = contentModifier
                        )
                        is MessagesTimelineItemRedactedContent -> MessagesTimelineItemRedactedView(
                            content = messageEvent.content,
                            modifier = contentModifier
                        )
                        is MessagesTimelineItemTextBasedContent -> MessagesTimelineItemTextView(
                            content = messageEvent.content,
                            interactionSource = interactionSource,
                            modifier = contentModifier,
                            onTextClicked = onClick,
                            onTextLongClicked = onLongClick
                        )
                        is MessagesTimelineItemUnknownContent -> MessagesTimelineItemUnknownView(
                            content = messageEvent.content,
                            modifier = contentModifier
                        )
                        is MessagesTimelineItemImageContent -> MessagesTimelineItemImageView(
                            content = messageEvent.content,
                            modifier = contentModifier
                        )
                    }
                }
                MessagesReactionsView(
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
    timelineItems: ImmutableList<MessagesTimelineItemState>,
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

class MessagesItemGroupPositionToMessagesTimelineItemContentProvider :
    PairCombinedPreviewParameter<MessagesItemGroupPosition, MessagesTimelineItemContent>(
        MessagesItemGroupPositionProvider() to MessagesTimelineItemContentProvider()
    )

@Suppress("PreviewPublic")
@Preview(showBackground = true)
@Composable
fun TimelineItemsPreview(
    @PreviewParameter(MessagesTimelineItemContentProvider::class)
    content: MessagesTimelineItemContent
) {
    val timelineItems = persistentListOf(
        // 3 items (First Middle Last) with isMine = false
        createMessageEvent(
            isMine = false,
            content = content,
            groupPosition = MessagesItemGroupPosition.First
        ),
        createMessageEvent(
            isMine = false,
            content = content,
            groupPosition = MessagesItemGroupPosition.Middle
        ),
        createMessageEvent(
            isMine = false,
            content = content,
            groupPosition = MessagesItemGroupPosition.Last
        ),
        // 3 items (First Middle Last) with isMine = true
        createMessageEvent(
            isMine = true,
            content = content,
            groupPosition = MessagesItemGroupPosition.First
        ),
        createMessageEvent(
            isMine = true,
            content = content,
            groupPosition = MessagesItemGroupPosition.Middle
        ),
        createMessageEvent(
            isMine = true,
            content = content,
            groupPosition = MessagesItemGroupPosition.Last
        ),
    )
    TimelineView(
        state = TimelineState(
            timelineItems = Async.Success(timelineItems)
        )
    )
}

private fun createMessageEvent(
    isMine: Boolean,
    content: MessagesTimelineItemContent,
    groupPosition: MessagesItemGroupPosition
): MessagesTimelineItemState {
    return MessagesTimelineItemState.MessageEvent(
        id = Math.random().toString(),
        senderId = "senderId",
        senderAvatar = AvatarData("sender"),
        content = content,
        reactionsState = MessagesItemReactionState(
            listOf(
                AggregatedReaction("👍", "1")
            )
        ),
        isMine = isMine,
        senderDisplayName = "sender",
        groupPosition = groupPosition,
    )
}
