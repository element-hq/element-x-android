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

import android.view.accessibility.AccessibilityManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.timeline.components.TimelineItemRow
import io.element.android.features.messages.impl.timeline.di.LocalTimelineItemPresenterFactories
import io.element.android.features.messages.impl.timeline.di.aFakeTimelineItemPresenterFactories
import io.element.android.features.messages.impl.timeline.model.NewEventState
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContentProvider
import io.element.android.features.messages.impl.typing.TypingNotificationState
import io.element.android.features.messages.impl.typing.aTypingNotificationState
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.FloatingActionButton
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.abs

@Composable
fun TimelineView(
    state: TimelineState,
    typingNotificationState: TypingNotificationState,
    onUserDataClicked: (UserId) -> Unit,
    onLinkClicked: (String) -> Unit,
    onMessageClicked: (TimelineItem.Event) -> Unit,
    onMessageLongClicked: (TimelineItem.Event) -> Unit,
    onTimestampClicked: (TimelineItem.Event) -> Unit,
    onSwipeToReply: (TimelineItem.Event) -> Unit,
    onReactionClicked: (emoji: String, TimelineItem.Event) -> Unit,
    onReactionLongClicked: (emoji: String, TimelineItem.Event) -> Unit,
    onMoreReactionsClicked: (TimelineItem.Event) -> Unit,
    onReadReceiptClick: (TimelineItem.Event) -> Unit,
    modifier: Modifier = Modifier,
    forceJumpToBottomVisibility: Boolean = false
) {

    fun clearFocusRequestState() {
        state.eventSink(TimelineEvents.ClearFocusRequestState)
    }

    fun onScrollFinishedAt(firstVisibleIndex: Int) {
        state.eventSink(TimelineEvents.OnScrollFinished(firstVisibleIndex))
    }

    val context = LocalContext.current
    val lazyListState = rememberLazyListState()
    // Disable reverse layout when TalkBack is enabled to avoid incorrect ordering issues seen in the current Compose UI version
    val useReverseLayout = remember {
        val accessibilityManager = context.getSystemService(AccessibilityManager::class.java)
        accessibilityManager.isTouchExplorationEnabled.not()
    }

    @Suppress("UNUSED_PARAMETER")
    fun inReplyToClicked(eventId: EventId) {
        // TODO implement this logic once we have support to 'jump to event X' in sliding sync
    }

    LaunchedEffect(key1 = state.timelineItems) {
        Timber.d("TimelineView - timelineItem identifiers: ${state.timelineItems.joinToString(", ") { it.identifier() }}")
    }

    // Animate alpha when timeline is first displayed, to avoid flashes or glitching when viewing rooms
    AnimatedVisibility(visible = true, enter = fadeIn()) {
        Box(modifier) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState,
                reverseLayout = useReverseLayout,
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                /*
                item(key = UUID.randomUUID()) {
                    TypingNotificationView(state = typingNotificationState)
                }

                 */
                items(
                    items = state.timelineItems,
                    contentType = { timelineItem -> timelineItem.contentType() },
                    key = { timelineItem -> timelineItem.identifier() },
                ) { timelineItem ->
                    TimelineItemRow(
                        timelineItem = timelineItem,
                        timelineRoomInfo = state.timelineRoomInfo,
                        renderReadReceipts = state.renderReadReceipts,
                        isLastOutgoingMessage = (timelineItem as? TimelineItem.Event)?.isMine == true &&
                            state.timelineItems.first().identifier() == timelineItem.identifier(),
                        highlightedItem = state.highlightedEventId?.value,
                        onClick = onMessageClicked,
                        onLongClick = onMessageLongClicked,
                        onUserDataClick = onUserDataClicked,
                        onLinkClicked = onLinkClicked,
                        inReplyToClick = ::inReplyToClicked,
                        onReactionClick = onReactionClicked,
                        onReactionLongClick = onReactionLongClicked,
                        onMoreReactionsClick = onMoreReactionsClicked,
                        onReadReceiptClick = onReadReceiptClick,
                        onTimestampClicked = onTimestampClicked,
                        eventSink = state.eventSink,
                        onSwipeToReply = onSwipeToReply,
                    )
                }
            }

            FocusRequestStateView(
                focusRequestState = state.focusRequestState,
                onClearFocusRequestState = ::clearFocusRequestState
            )

            TimelineScrollHelper(
                isTimelineEmpty = state.timelineItems.isEmpty(),
                lazyListState = lazyListState,
                forceJumpToBottomVisibility = forceJumpToBottomVisibility,
                newEventState = state.newEventState,
                isLive = state.isLive,
                focusRequestState = state.focusRequestState,
                onScrollFinishedAt = ::onScrollFinishedAt,
                onClearFocusRequestState = ::clearFocusRequestState,
                onJumpToLive = { state.eventSink(TimelineEvents.JumpToLive) },
            )
        }
    }
}

@Composable
private fun FocusRequestStateView(
    focusRequestState: FocusRequestState,
    onClearFocusRequestState: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(enabled = focusRequestState is FocusRequestState.Fetching) {
        onClearFocusRequestState()
    }

    when (focusRequestState) {
        is FocusRequestState.Failure -> {
            ErrorDialog(
                content = stringResource(id = CommonStrings.common_failed),
                onDismiss = onClearFocusRequestState,
                modifier = modifier,
            )
        }
        FocusRequestState.Fetching -> {
            ProgressDialog(modifier = modifier)
        }
        is FocusRequestState.Cached, FocusRequestState.None -> Unit
    }
}

@Composable
private fun BoxScope.TimelineScrollHelper(
    isTimelineEmpty: Boolean,
    lazyListState: LazyListState,
    newEventState: NewEventState,
    isLive: Boolean,
    forceJumpToBottomVisibility: Boolean,
    focusRequestState: FocusRequestState,
    onClearFocusRequestState: () -> Unit,
    onScrollFinishedAt: (Int) -> Unit,
    onJumpToLive: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val isScrollFinished by remember { derivedStateOf { !lazyListState.isScrollInProgress } }
    val canAutoScroll by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex < 3 && isLive
        }
    }

    fun scrollToBottom() {
        coroutineScope.launch {
            if (lazyListState.firstVisibleItemIndex > 10) {
                lazyListState.scrollToItem(0)
            } else {
                lazyListState.animateScrollToItem(0)
            }
        }
    }

    fun jumpToBottom() {
        if (isLive) {
            scrollToBottom()
        } else {
            onJumpToLive()
        }
    }

    LaunchedEffect(key1 = focusRequestState) {
        if (focusRequestState is FocusRequestState.Cached) {
            if (abs(lazyListState.firstVisibleItemIndex - focusRequestState.index) < 10) {
                lazyListState.animateScrollToItem(focusRequestState.index)
            } else {
                lazyListState.scrollToItem(focusRequestState.index)
            }
            onClearFocusRequestState()
        }
    }

    LaunchedEffect(canAutoScroll, newEventState) {
        Timber.d("TimelineScrollHelper - canAutoScroll: $canAutoScroll, newEventState: $newEventState")
        val shouldScrollToBottom = isScrollFinished && (canAutoScroll || newEventState == NewEventState.FromMe)
        if (shouldScrollToBottom) {
            scrollToBottom()
        }
    }

    val latestOnScrollFinishedAt by rememberUpdatedState(onScrollFinishedAt)
    LaunchedEffect(isScrollFinished, isTimelineEmpty) {
        if (isScrollFinished && !isTimelineEmpty) {
            // Notify the parent composable about the first visible item index when scrolling finishes
            latestOnScrollFinishedAt(lazyListState.firstVisibleItemIndex)
        }
    }

    JumpToBottomButton(
        // Use inverse of canAutoScroll otherwise we might briefly see the before the scroll animation is triggered
        isVisible = !canAutoScroll || forceJumpToBottomVisibility || !isLive,
        modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 12.dp),
        onClick = { jumpToBottom() },
    )
}

@Composable
private fun JumpToBottomButton(
    isVisible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible,
        enter = scaleIn(animationSpec = tween(100)),
        exit = scaleOut(animationSpec = tween(100)),
    ) {
        FloatingActionButton(
            onClick = onClick,
            elevation = FloatingActionButtonDefaults.elevation(4.dp, 4.dp, 4.dp, 4.dp),
            shape = CircleShape,
            modifier = Modifier.size(36.dp),
            containerColor = ElementTheme.colors.bgSubtleSecondary,
            contentColor = ElementTheme.colors.iconSecondary
        ) {
            Icon(
                modifier = Modifier
                        .size(24.dp)
                        .rotate(90f),
                imageVector = CompoundIcons.ArrowRight(),
                contentDescription = stringResource(id = CommonStrings.a11y_jump_to_bottom)
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineViewPreview(
    @PreviewParameter(TimelineItemEventContentProvider::class) content: TimelineItemEventContent
) = ElementPreview {
    val timelineItems = aTimelineItemList(content)
    CompositionLocalProvider(
        LocalTimelineItemPresenterFactories provides aFakeTimelineItemPresenterFactories(),
    ) {
        TimelineView(
            state = aTimelineState(timelineItems),
            typingNotificationState = aTypingNotificationState(),
            onUserDataClicked = {},
            onLinkClicked = {},
            onMessageClicked = {},
            onMessageLongClicked = {},
            onTimestampClicked = {},
            onSwipeToReply = {},
            onReactionClicked = { _, _ -> },
            onReactionLongClicked = { _, _ -> },
            onMoreReactionsClicked = {},
            onReadReceiptClick = {},
            forceJumpToBottomVisibility = true,
        )
    }
}
