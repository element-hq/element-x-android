@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)

package io.element.android.x.features.messages

import Avatar
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Alignment.Companion.Start
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import io.element.android.x.core.compose.LogCompositions
import io.element.android.x.core.data.StableCharSequence
import io.element.android.x.designsystem.components.avatar.AvatarData
import io.element.android.x.features.messages.components.*
import io.element.android.x.features.messages.model.MessagesItemGroupPosition
import io.element.android.x.features.messages.model.MessagesTimelineItemState
import io.element.android.x.features.messages.model.MessagesViewState
import io.element.android.x.features.messages.model.content.*
import io.element.android.x.features.messages.textcomposer.MessageComposerViewModel
import io.element.android.x.features.messages.textcomposer.MessageComposerViewState
import io.element.android.x.textcomposer.TextComposer
import kotlinx.coroutines.launch
import timber.log.Timber

private val BUBBLE_RADIUS = 16.dp
private val COMPOSER_HEIGHT = 112.dp

@Composable
fun MessagesScreen(
    roomId: String,
    onBackPressed: () -> Unit
) {
    val viewModel: MessagesViewModel = mavericksViewModel(argsFactory = { roomId })
    val composerViewModel: MessageComposerViewModel = mavericksViewModel(argsFactory = { roomId })

    fun onSendMessage(textMessage: String) {
        viewModel.sendMessage(textMessage)
        composerViewModel.updateText("")
    }

    LogCompositions(tag = "MessagesScreen", msg = "Root")
    val actionsSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
    )
    val coroutineScope = rememberCoroutineScope()
    val roomTitle by viewModel.collectAsState(MessagesViewState::roomName)
    val roomAvatar by viewModel.collectAsState(MessagesViewState::roomAvatar)
    val timelineItems by viewModel.collectAsState(MessagesViewState::timelineItems)
    val hasMoreToLoad by viewModel.collectAsState(MessagesViewState::hasMoreToLoad)
    val snackBarContent by viewModel.collectAsState(MessagesViewState::snackbarContent)
    val composerFullScreen by composerViewModel.collectAsState(MessageComposerViewState::isFullScreen)
    val composerCanSendMessage by composerViewModel.collectAsState(MessageComposerViewState::isSendButtonVisible)
    val composerText by composerViewModel.collectAsState(MessageComposerViewState::text)
    val snackbarHostState = remember { SnackbarHostState() }
    MessagesContent(
        roomTitle = roomTitle,
        roomAvatar = roomAvatar,
        timelineItems = timelineItems().orEmpty(),
        hasMoreToLoad = hasMoreToLoad,
        onReachedLoadMore = viewModel::loadMore,
        onBackPressed = onBackPressed,
        onSendMessage = ::onSendMessage,
        composerFullScreen = composerFullScreen,
        onComposerFullScreenChange = composerViewModel::onComposerFullScreenChange,
        onComposerTextChange = composerViewModel::updateText,
        composerCanSendMessage = composerCanSendMessage,
        composerText = composerText,
        onClick = {
            Timber.v("onClick on timeline item: ${it.id}")
        },
        onLongClick = {
            viewModel.computeActionsSheetState(it)
            coroutineScope.launch {
                actionsSheetState.show()
            }
        },
        snackbarHostState = snackbarHostState,
    )
    val itemActionsSheetState by viewModel.collectAsState(prop1 = MessagesViewState::itemActionsSheetState)
    TimelineItemActionsScreen(
        sheetState = actionsSheetState,
        actionsSheetState = itemActionsSheetState(),
        onActionClicked = {
            viewModel.handleItemAction(it)
            coroutineScope.launch {
                actionsSheetState.hide()
            }
        }
    )
    snackBarContent?.let {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(it)
        }
        viewModel.onSnackbarShown()
    }
}

@Composable
fun MessagesContent(
    roomTitle: String?,
    roomAvatar: AvatarData?,
    timelineItems: List<MessagesTimelineItemState>,
    hasMoreToLoad: Boolean,
    onReachedLoadMore: () -> Unit,
    onBackPressed: () -> Unit,
    onSendMessage: (String) -> Unit,
    onClick: (MessagesTimelineItemState.MessageEvent) -> Unit,
    onLongClick: (MessagesTimelineItemState.MessageEvent) -> Unit,
    composerFullScreen: Boolean,
    onComposerFullScreenChange: () -> Unit,
    onComposerTextChange: (CharSequence) -> Unit,
    composerCanSendMessage: Boolean,
    composerText: StableCharSequence?,
    snackbarHostState: SnackbarHostState,
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
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                if (!composerFullScreen) {
                    TimelineItems(
                        lazyListState = lazyListState,
                        timelineItems = timelineItems,
                        hasMoreToLoad = hasMoreToLoad,
                        onReachedLoadMore = onReachedLoadMore,
                        modifier = Modifier.weight(1f),
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                }
                TextComposer(
                    onSendMessage = onSendMessage,
                    fullscreen = composerFullScreen,
                    onFullscreenToggle = onComposerFullScreenChange,
                    onComposerTextChange = onComposerTextChange,
                    composerCanSendMessage = composerCanSendMessage,
                    composerText = composerText?.charSequence?.toString(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .let {
                            if (composerFullScreen) {
                                it.weight(1f)
                            } else {
                                it.height(COMPOSER_HEIGHT)
                            }
                        },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    )
}

@Composable
fun TimelineItems(
    lazyListState: LazyListState,
    timelineItems: List<MessagesTimelineItemState>,
    hasMoreToLoad: Boolean,
    onClick: (MessagesTimelineItemState.MessageEvent) -> Unit,
    onLongClick: ((MessagesTimelineItemState.MessageEvent)) -> Unit,
    onReachedLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = lazyListState,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Bottom,
        reverseLayout = true
    ) {
        itemsIndexed(timelineItems) { index, timelineItem ->
            TimelineItemRow(
                timelineItem = timelineItem,
                onClick = onClick,
                onLongClick = onLongClick
            )
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
    timelineItem: MessagesTimelineItemState,
    onClick: (MessagesTimelineItemState.MessageEvent) -> Unit,
    onLongClick: (MessagesTimelineItemState.MessageEvent) -> Unit,
) {
    when (timelineItem) {
        is MessagesTimelineItemState.Virtual -> return
        is MessagesTimelineItemState.MessageEvent -> MessageEventRow(
            messageEvent = timelineItem,
            onClick = { onClick(timelineItem) },
            onLongClick = { onLongClick(timelineItem) }
        )
    }
}

@Composable
fun MessageEventRow(
    messageEvent: MessagesTimelineItemState.MessageEvent,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (parentAlignment, contentAlignment) = if (messageEvent.isMine) {
        Pair(Alignment.CenterEnd, End)
    } else {
        Pair(Alignment.CenterStart, Start)
    }
    Box(
        modifier = Modifier
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
                            modifier = contentModifier
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
        Spacer(modifier = Modifier.height(8.dp))
    } else {
        Spacer(modifier = Modifier.height(2.dp))
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageEventBubble(
    groupPosition: MessagesItemGroupPosition,
    isMine: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    fun bubbleShape(): Shape {
        return when (groupPosition) {
            MessagesItemGroupPosition.First -> if (isMine) {
                RoundedCornerShape(BUBBLE_RADIUS, BUBBLE_RADIUS, 0.dp, BUBBLE_RADIUS)
            } else {
                RoundedCornerShape(BUBBLE_RADIUS, BUBBLE_RADIUS, BUBBLE_RADIUS, 0.dp)
            }
            MessagesItemGroupPosition.Middle -> if (isMine) {
                RoundedCornerShape(BUBBLE_RADIUS, 0.dp, 0.dp, BUBBLE_RADIUS)
            } else {
                RoundedCornerShape(0.dp, BUBBLE_RADIUS, BUBBLE_RADIUS, 0.dp)
            }
            MessagesItemGroupPosition.Last -> if (isMine) {
                RoundedCornerShape(BUBBLE_RADIUS, 0.dp, BUBBLE_RADIUS, BUBBLE_RADIUS)
            } else {
                RoundedCornerShape(0.dp, BUBBLE_RADIUS, BUBBLE_RADIUS, BUBBLE_RADIUS)
            }
            MessagesItemGroupPosition.None ->
                RoundedCornerShape(
                    BUBBLE_RADIUS,
                    BUBBLE_RADIUS,
                    BUBBLE_RADIUS,
                    BUBBLE_RADIUS
                )
        }
    }

    fun Modifier.offsetForItem(): Modifier {
        return if (isMine) {
            offset(y = -(12.dp))
        } else {
            offset(x = 20.dp, y = -(12.dp))
        }
    }

    val (backgroundBubbleColor, border) = if (isMine) {
        Pair(MaterialTheme.colorScheme.surfaceVariant, null)
    } else {
        Pair(
            Color.Transparent,
            BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        )
    }
    val bubbleShape = bubbleShape()
    Surface(
        modifier = modifier
            .widthIn(min = 80.dp)
            .offsetForItem()
            .clip(bubbleShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() }
            ),
        color = backgroundBubbleColor,
        shape = bubbleShape,
        border = border,
        content = content
    )
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
        CircularProgressIndicator(
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )
        LaunchedEffect(Unit) {
            onReachedLoadMore()
        }
    }

}