@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.x.features.messages

import Avatar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import io.element.android.x.core.data.LogCompositions
import io.element.android.x.designsystem.components.avatar.AvatarData
import io.element.android.x.features.messages.model.MessagesViewState
import io.element.android.x.matrix.timeline.MatrixTimelineItem

@Composable
fun MessagesScreen(roomId: String) {
    val viewModel: MessagesViewModel = mavericksViewModel(argsFactory = { roomId })
    LogCompositions(tag = "MessagesScreen", msg = "Root")
    val roomTitle by viewModel.collectAsState(MessagesViewState::roomName)
    val roomAvatar by viewModel.collectAsState(MessagesViewState::roomAvatar)
    val timelineItems by viewModel.collectAsState(MessagesViewState::timelineItems)
    MessagesContent(roomTitle, roomAvatar, timelineItems().orEmpty())
}

@Composable
fun MessagesContent(
    roomTitle: String?,
    roomAvatar: AvatarData?,
    timelineItems: List<MatrixTimelineItem>,
) {
    LogCompositions(tag = "MessagesScreen", msg = "Content")
    val lazyListState = rememberLazyListState()
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (roomAvatar != null) {
                        IconButton(onClick = {}) {
                            Avatar(roomAvatar)
                        }
                    }
                },
                title = { Text(text = roomTitle ?: "") }
            )
        },
        content = { padding ->
            TimelineItems(
                padding = padding,
                lazyListState = lazyListState,
                timelineItems = timelineItems
            )
        }
    )
}

@Composable
fun TimelineItems(
    padding: PaddingValues,
    lazyListState: LazyListState,
    timelineItems: List<MatrixTimelineItem>
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
        items(timelineItems) { timelineItem ->
            TimelineItemRow(timelineItem = timelineItem)
        }
    }
}

@Composable
fun TimelineItemRow(
    timelineItem: MatrixTimelineItem
) {
    when (timelineItem) {
        MatrixTimelineItem.Other -> return
        MatrixTimelineItem.Virtual -> return
        is MatrixTimelineItem.Event -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = { },
                        indication = rememberRipple(),
                        interactionSource = remember { MutableInteractionSource() }
                    ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        fontSize = 14.sp,
                        text = timelineItem.event.raw() ?: "",
                    )
                }
            }
        }

    }
}

@Composable
internal fun MessagesLoadingMoreIndicator() {
    Box(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
    }
}

