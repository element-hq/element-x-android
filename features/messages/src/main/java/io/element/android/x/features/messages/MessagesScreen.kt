@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)

package io.element.android.x.features.messages

import Avatar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Alignment.Companion.Start
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import io.element.android.x.core.compose.LogCompositions
import io.element.android.x.core.compose.PairCombinedPreviewParameter
import io.element.android.x.core.data.StableCharSequence
import io.element.android.x.designsystem.components.avatar.AvatarData
import io.element.android.x.features.messages.components.*
import io.element.android.x.features.messages.model.*
import io.element.android.x.features.messages.model.content.*
import io.element.android.x.features.messages.textcomposer.MessageComposerViewModel
import io.element.android.x.features.messages.textcomposer.MessageComposerViewState
import io.element.android.x.textcomposer.MessageComposerMode
import io.element.android.x.textcomposer.TextComposer
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Math.random

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
    val composerMode by viewModel.collectAsState(MessagesViewState::composerMode)
    val composerFullScreen by composerViewModel.collectAsState(MessageComposerViewState::isFullScreen)
    val composerCanSendMessage by composerViewModel.collectAsState(MessageComposerViewState::isSendButtonVisible)
    val composerText by composerViewModel.collectAsState(MessageComposerViewState::text)
    val snackbarHostState = remember { SnackbarHostState() }
    MessagesScreenContent(
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
        composerMode = composerMode,
        onCloseSpecialMode = viewModel::setNormalMode,
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
                val targetEvent = viewModel.getTargetEvent()
                when (it) {
                    is MessagesItemAction.Edit -> {
                        // Entering Edit mode, update the text in the composer.
                        val newComposerText =
                            (targetEvent?.content as? MessagesTimelineItemTextBasedContent)?.body.orEmpty()
                        composerViewModel.updateText(newComposerText)
                    }
                    else -> Unit
                }
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
fun MessagesScreenContent(
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
    composerMode: MessageComposerMode,
    onCloseSpecialMode: () -> Unit,
    composerCanSendMessage: Boolean,
    composerText: StableCharSequence?,
    snackbarHostState: SnackbarHostState,
) {
    LogCompositions(tag = "MessagesScreen", msg = "Content")
    Scaffold(
        topBar = {
            MessagesTopAppBar(
                roomTitle = roomTitle,
                roomAvatar = roomAvatar,
                onBackPressed = onBackPressed
            )
        },
        content = { padding ->
            MessagesContent(
                modifier = Modifier.padding(padding),
                timelineItems = timelineItems,
                hasMoreToLoad = hasMoreToLoad,
                onReachedLoadMore = onReachedLoadMore,
                onSendMessage = onSendMessage,
                onClick = onClick,
                onLongClick = onLongClick,
                composerMode = composerMode,
                onCloseSpecialMode = onCloseSpecialMode,
                composerFullScreen = composerFullScreen,
                onComposerFullScreenChange = onComposerFullScreenChange,
                onComposerTextChange = onComposerTextChange,
                composerCanSendMessage = composerCanSendMessage,
                composerText = composerText
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    )
}

@Composable
fun MessagesContent(
    timelineItems: List<MessagesTimelineItemState>,
    hasMoreToLoad: Boolean,
    onReachedLoadMore: () -> Unit,
    onSendMessage: (String) -> Unit,
    onClick: (MessagesTimelineItemState.MessageEvent) -> Unit,
    onLongClick: (MessagesTimelineItemState.MessageEvent) -> Unit,
    composerMode: MessageComposerMode,
    onCloseSpecialMode: () -> Unit,
    composerFullScreen: Boolean,
    onComposerFullScreenChange: () -> Unit,
    onComposerTextChange: (CharSequence) -> Unit,
    composerCanSendMessage: Boolean,
    composerText: StableCharSequence?,
    modifier: Modifier = Modifier
) {

    val lazyListState = rememberLazyListState()
    Column(
        modifier = modifier
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
            composerMode = composerMode,
            onCloseSpecialMode = onCloseSpecialMode,
            onComposerTextChange = onComposerTextChange,
            composerCanSendMessage = composerCanSendMessage,
            composerText = composerText?.charSequence?.toString(),
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .let {
                    if (composerFullScreen) {
                        it.weight(1f)
                    } else {
                        it.height(COMPOSER_HEIGHT)
                    }
                },
        )
    }
}

@Composable
fun MessagesTopAppBar(
    roomTitle: String?,
    roomAvatar: AvatarData?,
    onBackPressed: () -> Unit,
) {
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
}

@Composable
fun TimelineItems(
    lazyListState: LazyListState,
    timelineItems: List<MessagesTimelineItemState>,
    modifier: Modifier = Modifier,
    hasMoreToLoad: Boolean = false,
    onClick: (MessagesTimelineItemState.MessageEvent) -> Unit = {},
    onLongClick: ((MessagesTimelineItemState.MessageEvent)) -> Unit = {},
    onReachedLoadMore: () -> Unit = {},
) {
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
                    onClick = onClick,
                    onLongClick = onLongClick
                )
            }
            if (hasMoreToLoad) {
                item {
                    MessagesLoadingMoreIndicator()
                }
            }
        }
        MessagesScrollHelper(
            lazyListState = lazyListState,
            timelineItems = timelineItems,
            onLoadMore = onReachedLoadMore
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
                    isHighlighted = messageEvent.isHighlighted,
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
                        else -> TODO() /* compiler issue ? */
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
internal fun BoxScope.MessagesScrollHelper(
    lazyListState: LazyListState,
    timelineItems: List<MessagesTimelineItemState>,
    onLoadMore: () -> Unit,
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
internal fun MessagesLoadingMoreIndicator() {
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

@Preview(showBackground = true)
@Composable
fun TimelineItemsPreview(
    @PreviewParameter(MessagesTimelineItemContentProvider::class)
    content: MessagesTimelineItemContent
) {
    TimelineItems(
        lazyListState = LazyListState(),
        timelineItems = listOf(
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
        ),
        hasMoreToLoad = true,
    )
}

private fun createMessageEvent(
    isMine: Boolean,
    content: MessagesTimelineItemContent,
    groupPosition: MessagesItemGroupPosition
): MessagesTimelineItemState {
    return MessagesTimelineItemState.MessageEvent(
        id = random().toString(),
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
