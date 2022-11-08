@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.x.features.messages

import Avatar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import io.element.android.x.core.data.LogCompositions
import io.element.android.x.designsystem.components.avatar.AvatarData
import io.element.android.x.features.messages.model.MessagesItemGroupPosition
import io.element.android.x.features.messages.model.MessagesTimelineItemState
import io.element.android.x.features.messages.model.MessagesViewState

@Composable
fun MessagesScreen(
    roomId: String,
    onBackPressed: () -> Unit
) {
    val viewModel: MessagesViewModel = mavericksViewModel(argsFactory = { roomId })
    LogCompositions(tag = "MessagesScreen", msg = "Root")
    val roomTitle by viewModel.collectAsState(MessagesViewState::roomName)
    val roomAvatar by viewModel.collectAsState(MessagesViewState::roomAvatar)
    val timelineItems by viewModel.collectAsState(MessagesViewState::timelineItems)
    val hasMoreToLoad by viewModel.collectAsState(MessagesViewState::hasMoreToLoad)
    MessagesContent(
        roomTitle = roomTitle,
        roomAvatar = roomAvatar,
        timelineItems = timelineItems().orEmpty(),
        hasMoreToLoad = hasMoreToLoad,
        onReachedLoadMore = viewModel::loadMore,
        onBackPressed = onBackPressed
    )
}

@Composable
fun MessagesContent(
    roomTitle: String?,
    roomAvatar: AvatarData?,
    timelineItems: List<MessagesTimelineItemState>,
    hasMoreToLoad: Boolean,
    onReachedLoadMore: () -> Unit,
    onBackPressed: () -> Unit
) {
    LogCompositions(tag = "MessagesScreen", msg = "Content")
    val lazyListState = rememberLazyListState()
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (roomAvatar != null) {
                            Avatar(roomAvatar)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            text = roomTitle ?: "Unknown room",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

            )
        },
        content = { padding ->
            TimelineItems(
                padding = padding,
                lazyListState = lazyListState,
                timelineItems = timelineItems,
                hasMoreToLoad = hasMoreToLoad,
                onReachedLoadMore = onReachedLoadMore,
            )
        }
    )
}

@Composable
fun TimelineItems(
    padding: PaddingValues,
    lazyListState: LazyListState,
    timelineItems: List<MessagesTimelineItemState>,
    hasMoreToLoad: Boolean,
    onReachedLoadMore: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize(),
        state = lazyListState,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Bottom,
        reverseLayout = true
    ) {
        itemsIndexed(timelineItems) { index, timelineItem ->
            TimelineItemRow(timelineItem = timelineItem)
        }
        if (hasMoreToLoad) {
            item {
                MessagesLoadingMoreIndicator(onReachedLoadMore)
            }
        }
    }
}


@Composable
fun TimelineItemRow(
    timelineItem: MessagesTimelineItemState
) {
    when (timelineItem) {
        is MessagesTimelineItemState.Virtual -> return
        is MessagesTimelineItemState.MessageEvent -> MessageEventRow(messageEvent = timelineItem)
    }
}

@Composable
fun MessageEventRow(
    messageEvent: MessagesTimelineItemState.MessageEvent,
    modifier: Modifier = Modifier
) {
    val contentAlignment = if (messageEvent.isMine) {
        Alignment.CenterEnd
    } else {
        Alignment.CenterStart
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = contentAlignment
    ) {
        Row(modifier = modifier
            .widthIn(max = 300.dp)
            .clickable(
                onClick = { },
                indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() }
            )) {
            if (!messageEvent.isMine) {
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column {
                if (messageEvent.showSenderInformation) {
                    MessageSenderInformation(messageEvent.sender, messageEvent.senderAvatar)
                }
                MessageEventBubble(messageEvent)
            }
            if (messageEvent.isMine) {
                Spacer(modifier = Modifier.width(16.dp))
            }
        }
    }
    if (messageEvent.groupPosition is MessagesItemGroupPosition.First) {
        Spacer(modifier = Modifier.height(8.dp))
    } else {
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun MessageSenderInformation(sender: String, senderAvatar: AvatarData?) {
    Row {
        if (senderAvatar != null) {
            Avatar(senderAvatar)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = sender,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .alignBy(LastBaseline)
                .paddingFrom(LastBaseline, after = 8.dp)
        )
    }
}

@Composable
fun MessageEventBubble(
    messageEvent: MessagesTimelineItemState.MessageEvent,
) {
    val backgroundBubbleColor = if (messageEvent.isMine) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.primary
    }
    Surface(
        color = backgroundBubbleColor,
        shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 20.dp),
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = messageEvent.content ?: "",
        )
    }
}

@Composable
internal fun MessagesLoadingMoreIndicator(onReachedLoadMore: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
        LaunchedEffect(Unit) {
            onReachedLoadMore()
        }
    }

}

