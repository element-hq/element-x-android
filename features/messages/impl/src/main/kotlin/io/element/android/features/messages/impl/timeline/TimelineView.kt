/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import android.view.accessibility.AccessibilityManager
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.crypto.sendfailure.resolve.ResolveVerifiedUserSendFailureView
import io.element.android.features.messages.impl.timeline.components.TimelineItemRow
import io.element.android.features.messages.impl.timeline.components.toText
import io.element.android.features.messages.impl.timeline.di.LocalTimelineItemPresenterFactories
import io.element.android.features.messages.impl.timeline.di.aFakeTimelineItemPresenterFactories
import io.element.android.features.messages.impl.timeline.focus.FocusRequestStateView
import io.element.android.features.messages.impl.timeline.model.NewEventState
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContentProvider
import io.element.android.features.messages.impl.timeline.protection.TimelineProtectionState
import io.element.android.features.messages.impl.timeline.protection.aTimelineProtectionState
import io.element.android.libraries.designsystem.components.dialogs.AlertDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.FloatingActionButton
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.utils.animateScrollToItemCenter
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.launch

@Composable
fun TimelineView(
    state: TimelineState,
    timelineProtectionState: TimelineProtectionState,
    onUserDataClick: (UserId) -> Unit,
    onLinkClick: (String) -> Unit,
    onMessageClick: (TimelineItem.Event) -> Unit,
    onMessageLongClick: (TimelineItem.Event) -> Unit,
    onSwipeToReply: (TimelineItem.Event) -> Unit,
    onReactionClick: (emoji: String, TimelineItem.Event) -> Unit,
    onReactionLongClick: (emoji: String, TimelineItem.Event) -> Unit,
    onMoreReactionsClick: (TimelineItem.Event) -> Unit,
    onReadReceiptClick: (TimelineItem.Event) -> Unit,
    onJoinCallClick: () -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    forceJumpToBottomVisibility: Boolean = false,
    nestedScrollConnection: NestedScrollConnection = rememberNestedScrollInteropConnection(),
) {
    fun clearFocusRequestState() {
        state.eventSink(TimelineEvents.ClearFocusRequestState)
    }

    fun onScrollFinishAt(firstVisibleIndex: Int) {
        state.eventSink(TimelineEvents.OnScrollFinished(firstVisibleIndex))
    }

    fun onFocusEventRender() {
        state.eventSink(TimelineEvents.OnFocusEventRender)
    }

    fun onJumpToLive() {
        state.eventSink(TimelineEvents.JumpToLive)
    }

    val context = LocalContext.current
    // Disable reverse layout when TalkBack is enabled to avoid incorrect ordering issues seen in the current Compose UI version
    val useReverseLayout = remember {
        val accessibilityManager = context.getSystemService(AccessibilityManager::class.java)
        accessibilityManager.isTouchExplorationEnabled.not()
    }

    fun inReplyToClick(eventId: EventId) {
        state.eventSink(TimelineEvents.FocusOnEvent(eventId))
    }

    // Animate alpha when timeline is first displayed, to avoid flashes or glitching when viewing rooms
    AnimatedVisibility(visible = true, enter = fadeIn()) {
        Box(modifier) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection),
                state = lazyListState,
                reverseLayout = useReverseLayout,
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(
                    items = state.timelineItems,
                    contentType = { timelineItem -> timelineItem.contentType() },
                    key = { timelineItem -> timelineItem.identifier() },
                ) { timelineItem ->
                    TimelineItemRow(
                        timelineItem = timelineItem,
                        timelineRoomInfo = state.timelineRoomInfo,
                        timelineProtectionState = timelineProtectionState,
                        renderReadReceipts = state.renderReadReceipts,
                        isLastOutgoingMessage = state.isLastOutgoingMessage(timelineItem.identifier()),
                        focusedEventId = state.focusedEventId,
                        onUserDataClick = onUserDataClick,
                        onLinkClick = onLinkClick,
                        onClick = onMessageClick,
                        onLongClick = onMessageLongClick,
                        inReplyToClick = ::inReplyToClick,
                        onReactionClick = onReactionClick,
                        onReactionLongClick = onReactionLongClick,
                        onMoreReactionsClick = onMoreReactionsClick,
                        onReadReceiptClick = onReadReceiptClick,
                        onSwipeToReply = onSwipeToReply,
                        onJoinCallClick = onJoinCallClick,
                        eventSink = state.eventSink,
                    )
                }
            }

            FocusRequestStateView(
                focusRequestState = state.focusRequestState,
                onClearFocusRequestState = ::clearFocusRequestState
            )

            TimelineScrollHelper(
                hasAnyEvent = state.hasAnyEvent,
                lazyListState = lazyListState,
                forceJumpToBottomVisibility = forceJumpToBottomVisibility,
                newEventState = state.newEventState,
                isLive = state.isLive,
                focusRequestState = state.focusRequestState,
                onScrollFinishAt = ::onScrollFinishAt,
                onJumpToLive = ::onJumpToLive,
                onFocusEventRender = ::onFocusEventRender,
            )
        }
    }

    ResolveVerifiedUserSendFailureView(state = state.resolveVerifiedUserSendFailureState)

    MessageShieldDialog(state)
}

@Composable
private fun MessageShieldDialog(state: TimelineState) {
    val messageShield = state.messageShield ?: return
    AlertDialog(
        content = messageShield.toText(),
        onDismiss = { state.eventSink.invoke(TimelineEvents.HideShieldDialog) },
    )
}

@Composable
private fun BoxScope.TimelineScrollHelper(
    hasAnyEvent: Boolean,
    lazyListState: LazyListState,
    newEventState: NewEventState,
    isLive: Boolean,
    forceJumpToBottomVisibility: Boolean,
    focusRequestState: FocusRequestState,
    onScrollFinishAt: (Int) -> Unit,
    onJumpToLive: () -> Unit,
    onFocusEventRender: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val isScrollFinished by remember { derivedStateOf { !lazyListState.isScrollInProgress } }
    val canAutoScroll by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex < 3 && isLive
        }
    }
    var jumpToLiveHandled by remember { mutableStateOf(true) }

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
            jumpToLiveHandled = false
            onJumpToLive()
        }
    }

    LaunchedEffect(jumpToLiveHandled, isLive) {
        if (!jumpToLiveHandled && isLive) {
            lazyListState.scrollToItem(0)
            jumpToLiveHandled = true
        }
    }

    val latestOnFocusEventRender by rememberUpdatedState(onFocusEventRender)
    LaunchedEffect(focusRequestState) {
        if (focusRequestState is FocusRequestState.Success && focusRequestState.isIndexed && !focusRequestState.rendered) {
            lazyListState.animateScrollToItemCenter(focusRequestState.index)
            latestOnFocusEventRender()
        }
    }

    LaunchedEffect(canAutoScroll, newEventState) {
        val shouldScrollToBottom = isScrollFinished && (canAutoScroll || newEventState == NewEventState.FromMe)
        if (shouldScrollToBottom) {
            scrollToBottom()
        }
    }

    val latestOnScrollFinishAt by rememberUpdatedState(onScrollFinishAt)
    LaunchedEffect(isScrollFinished, hasAnyEvent) {
        if (isScrollFinished && hasAnyEvent) {
            // Notify the parent composable about the first visible item index when scrolling finishes
            latestOnScrollFinishAt(lazyListState.firstVisibleItemIndex)
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
    val timelineEvents = timelineItems.filterIsInstance<TimelineItem.Event>()
    val lastEventIdFromMe = timelineEvents.firstOrNull { it.isMine }?.eventId
    val lastEventIdFromOther = timelineEvents.firstOrNull { !it.isMine }?.eventId
    CompositionLocalProvider(
        LocalTimelineItemPresenterFactories provides aFakeTimelineItemPresenterFactories(),
    ) {
        TimelineView(
            state = aTimelineState(
                timelineItems = timelineItems,
                timelineRoomInfo = aTimelineRoomInfo(
                    pinnedEventIds = listOfNotNull(lastEventIdFromMe, lastEventIdFromOther)
                ),
                focusedEventIndex = 0,
            ),
            timelineProtectionState = aTimelineProtectionState(),
            onUserDataClick = {},
            onLinkClick = {},
            onMessageClick = {},
            onMessageLongClick = {},
            onSwipeToReply = {},
            onReactionClick = { _, _ -> },
            onReactionLongClick = { _, _ -> },
            onMoreReactionsClick = {},
            onReadReceiptClick = {},
            onJoinCallClick = {},
            forceJumpToBottomVisibility = true,
        )
    }
}
