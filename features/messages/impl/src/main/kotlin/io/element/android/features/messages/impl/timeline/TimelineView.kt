/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.crypto.sendfailure.resolve.ResolveVerifiedUserSendFailureView
import io.element.android.features.messages.impl.timeline.components.FloatingDateBadgeOverlay
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
import io.element.android.libraries.androidutils.system.copyToClipboard
import io.element.android.libraries.designsystem.components.dialogs.AlertDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.roundToPx
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.animateScrollToItemCenter
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.testtags.TestTag
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.libraries.ui.utils.time.isTalkbackActive
import io.element.android.wysiwyg.link.Link
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun TimelineView(
    state: TimelineState,
    timelineProtectionState: TimelineProtectionState,
    onUserDataClick: (MatrixUser) -> Unit,
    onLinkClick: (Link) -> Unit,
    onContentClick: (TimelineItem.Event) -> Unit,
    onMessageLongClick: (TimelineItem.Event) -> Unit,
    onSwipeToReply: (TimelineItem.Event) -> Unit,
    onReactionClick: (emoji: String, TimelineItem.Event) -> Unit,
    onReactionLongClick: (emoji: String, TimelineItem.Event) -> Unit,
    onMoreReactionsClick: (TimelineItem.Event) -> Unit,
    onReadReceiptClick: (TimelineItem.Event) -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    forceJumpToBottomVisibility: Boolean = false,
    forceJumpToReadMarkerVisibility: Boolean = false,
    nestedScrollConnection: NestedScrollConnection = rememberNestedScrollInteropConnection(),
    floatingDateTopOffset: Dp = 0.dp,
) {
    fun clearFocusRequestState() {
        state.eventSink(TimelineEvent.ClearFocusRequestState)
    }

    fun onScrollFinishAt(firstVisibleIndex: Int) {
        state.eventSink(TimelineEvent.OnScrollFinished(firstVisibleIndex))
    }

    fun onFocusEventRender() {
        state.eventSink(TimelineEvent.OnFocusEventRender)
    }

    fun onJumpToLive() {
        state.eventSink(TimelineEvent.JumpToLive)
    }

    fun onMarkAllAsRead() {
        state.eventSink(TimelineEvent.MarkAllAsRead)
    }

    val context = LocalContext.current
    val toastMessage = stringResource(CommonStrings.common_copied_to_clipboard)
    val view = LocalView.current
    // Disable reverse layout when TalkBack is enabled to avoid incorrect ordering issues seen in the current Compose UI version
    val useReverseLayout = !isTalkbackActive()

    fun inReplyToClick(eventId: EventId) {
        state.eventSink(TimelineEvent.FocusOnEvent(eventId))
    }

    fun onLinkLongClick(link: Link) {
        view.performHapticFeedback(
            HapticFeedbackConstants.LONG_PRESS
        )
        context.copyToClipboard(
            text = link.url,
            toastMessage = toastMessage,
        )
    }

    fun prefetchMoreItems() {
        state.eventSink(TimelineEvent.LoadMore(Timeline.PaginationDirection.BACKWARDS))
    }

    // Animate alpha when timeline is first displayed, to avoid flashes or glitching when viewing rooms
    AnimatedVisibility(visible = true, enter = fadeIn()) {
        Box(modifier) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection)
                    .testTag(TestTags.timeline),
                state = lazyListState,
                reverseLayout = useReverseLayout,
                contentPadding = PaddingValues(top = 64.dp, bottom = 8.dp),
            ) {
                items(
                    items = state.timelineItems,
                    contentType = { timelineItem -> timelineItem.contentType() },
                    key = { timelineItem -> timelineItem.identifier() },
                ) { timelineItem ->
                    TimelineItemRow(
                        timelineItem = timelineItem,
                        timelineMode = state.timelineMode,
                        timelineRoomInfo = state.timelineRoomInfo,
                        timelineProtectionState = timelineProtectionState,
                        renderReadReceipts = state.renderReadReceipts,
                        isLastOutgoingMessage = state.isLastOutgoingMessage(timelineItem.identifier()),
                        focusedEventId = state.focusedEventId,
                        displayThreadSummaries = state.displayThreadSummaries,
                        onUserDataClick = onUserDataClick,
                        onLinkClick = onLinkClick,
                        onLinkLongClick = ::onLinkLongClick,
                        onContentClick = onContentClick,
                        onLongClick = onMessageLongClick,
                        inReplyToClick = ::inReplyToClick,
                        onReactionClick = onReactionClick,
                        onReactionLongClick = onReactionLongClick,
                        onMoreReactionsClick = onMoreReactionsClick,
                        onReadReceiptClick = onReadReceiptClick,
                        onSwipeToReply = onSwipeToReply,
                        eventSink = state.eventSink,
                    )
                }
            }

            FocusRequestStateView(
                focusRequestState = state.focusRequestState,
                onClearFocusRequestState = ::clearFocusRequestState
            )

            TimelinePrefetchingHelper(
                lazyListState = lazyListState,
                prefetch = ::prefetchMoreItems
            )

            TimelineScrollHelper(
                hasAnyEvent = state.hasAnyEvent,
                lazyListState = lazyListState,
                forceJumpToBottomVisibility = forceJumpToBottomVisibility,
                forceJumpToReadMarkerVisibility = forceJumpToReadMarkerVisibility,
                newEventState = state.newEventState,
                isLive = state.isLive,
                focusRequestState = state.focusRequestState,
                displayJumpToUnread = state.displayJumpToUnread,
                readMarkerIndex = state.readMarkerIndex,
                onScrollFinishAt = ::onScrollFinishAt,
                onJumpToLive = ::onJumpToLive,
                onFocusEventRender = ::onFocusEventRender,
                onMarkAllAsRead = ::onMarkAllAsRead,
            )

            if (state.displayFloatingDateBadge && useReverseLayout) {
                FloatingDateBadgeOverlay(
                    lazyListState = lazyListState,
                    timelineItems = state.timelineItems,
                    isLive = state.isLive,
                    topOffset = floatingDateTopOffset,
                )
            }
        }
    }

    ResolveVerifiedUserSendFailureView(state = state.resolveVerifiedUserSendFailureState)

    MessageShieldDialog(state)
}

@Composable
private fun MessageShieldDialog(state: TimelineState) {
    val messageShield = state.messageShieldDialogData ?: return
    AlertDialog(
        content = messageShield.toText(),
        onDismiss = { state.eventSink.invoke(TimelineEvent.HideShieldDialog) },
    )
}

@Composable
private fun TimelinePrefetchingHelper(
    lazyListState: LazyListState,
    prefetch: () -> Unit,
) {
    val latestPrefetch by rememberUpdatedState(prefetch)

    LaunchedEffect(Unit) {
        // We're using snapshot flows for these because using `LaunchedEffect` with `derivedState` doesn't seem to be responsive enough
        val firstVisibleItemIndexFlow = snapshotFlow { lazyListState.firstVisibleItemIndex }
        val layoutInfoFlow = snapshotFlow { lazyListState.layoutInfo }
        val isScrollingFlow = snapshotFlow { lazyListState.isScrollInProgress }
            // This value changes too frequently, so we debounce it to avoid unnecessary prefetching. It's the equivalent of a conditional 'throttleLatest'
            .conflate()
            .transform { isScrolling ->
                emit(isScrolling)
                if (isScrolling) delay(100.milliseconds)
            }

        val isCloseToStartOfLoadedTimelineFlow = combine(layoutInfoFlow, firstVisibleItemIndexFlow) { layoutInfo, firstVisibleItemIndex ->
            firstVisibleItemIndex + layoutInfo.visibleItemsInfo.size >= layoutInfo.totalItemsCount - 40
        }

        // If we have no timeline items, we need to back paginate to load some messages. This usually happens on all timelines except for live ones.
        // This automatic pagination was previously done by the SDK, and we received a `Reset` update, but now we need to do it ourselves.
        val isEmptyTimelineFlow = layoutInfoFlow.map { it.totalItemsCount == 0 }

        combine(
            isCloseToStartOfLoadedTimelineFlow.distinctUntilChanged(),
            isScrollingFlow.distinctUntilChanged(),
            isEmptyTimelineFlow,
        ) { needsPrefetch, isScrolling, isEmptyAndNeedsBackPagination ->
            isEmptyAndNeedsBackPagination || needsPrefetch && isScrolling
        }
            .distinctUntilChanged()
            .collectLatest { needsPrefetch ->
                if (needsPrefetch) {
                    Timber.d("Prefetching pagination with ${lazyListState.layoutInfo.totalItemsCount} items")
                    latestPrefetch()
                }
            }
    }
}

@Composable
private fun BoxScope.TimelineScrollHelper(
    hasAnyEvent: Boolean,
    lazyListState: LazyListState,
    newEventState: NewEventState,
    isLive: Boolean,
    forceJumpToBottomVisibility: Boolean,
    forceJumpToReadMarkerVisibility: Boolean,
    focusRequestState: FocusRequestState,
    displayJumpToUnread: Boolean,
    readMarkerIndex: Int?,
    onScrollFinishAt: (Int) -> Unit,
    onJumpToLive: () -> Unit,
    onFocusEventRender: () -> Unit,
    onMarkAllAsRead: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val isScrollFinished by remember { derivedStateOf { !lazyListState.isScrollInProgress } }
    val canAutoScroll by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex < 3 && isLive
        }
    }
    val isJumpToUnreadVisible by remember {
        derivedStateOf {
            if (forceJumpToReadMarkerVisibility) return@derivedStateOf true
            val markerIndex = readMarkerIndex ?: return@derivedStateOf false
            val lastVisibleIndex = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            markerIndex > lastVisibleIndex
        }
    }
    val isJumpToBottomVisible = !canAutoScroll || forceJumpToBottomVisibility || !isLive
    var jumpToLiveHandled by remember { mutableStateOf(true) }

    /**
     * @param force If true, scroll to the bottom even if the user is already seeing the most recent item.
     * This fixes the issue where the user is seeing typing notification and so the read receipt is not sent
     * when a new message comes in.
     */
    fun scrollToBottom(force: Boolean) {
        coroutineScope.launch {
            if (lazyListState.firstVisibleItemIndex > 10) {
                lazyListState.scrollToItem(0)
            } else if (force || lazyListState.firstVisibleItemIndex != 0) {
                lazyListState.animateScrollToItem(0)
            }
        }
    }

    fun jumpToBottom() {
        if (isLive) {
            scrollToBottom(force = false)
        } else {
            jumpToLiveHandled = false
            onJumpToLive()
        }
    }

    fun jumpToReadMarker() {
        val markerIndex = readMarkerIndex ?: return
        coroutineScope.launch {
            lazyListState.animateScrollToItemCenter(markerIndex)
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
        val shouldScrollToBottom = isScrollFinished && when (newEventState) {
            is NewEventState.FromOther -> canAutoScroll
            NewEventState.FromMe -> true
            NewEventState.None -> false
        }
        if (shouldScrollToBottom) {
            scrollToBottom(force = true)
        }
    }

    val latestOnScrollFinishAt by rememberUpdatedState(onScrollFinishAt)
    LaunchedEffect(isScrollFinished, hasAnyEvent) {
        if (isScrollFinished && hasAnyEvent) {
            // Notify the parent composable about the first visible item index when scrolling finishes
            latestOnScrollFinishAt(lazyListState.firstVisibleItemIndex)
        }
    }

    Column(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 24.dp, bottom = 24.dp)
    ) {
        JumpToPositionButton(
            icon = CompoundIcons.ChevronUp(),
            contentDescription = stringResource(id = CommonStrings.a11y_jump_to_unread_messages),
            modifier = Modifier.padding(bottom = if (isJumpToBottomVisible) 12.dp else 0.dp),
            isVisible = isJumpToUnreadVisible,
            hasUnread = true,
            onClick = ::jumpToReadMarker,
            onMarkAsRead = onMarkAllAsRead,
            testTag = TestTags.jumpToUnreadButton,
        )
        JumpToPositionButton(
            icon = CompoundIcons.ChevronDown(),
            contentDescription = stringResource(id = CommonStrings.a11y_jump_to_bottom),
            isVisible = isJumpToBottomVisible,
            hasUnread = displayJumpToUnread && newEventState is NewEventState.FromOther,
            onClick = ::jumpToBottom,
            onMarkAsRead = onMarkAllAsRead,
            testTag = TestTags.jumpToBottomButton,
            dotAlignment = Alignment.BottomCenter,
        )
    }
}

@Composable
private fun JumpToPositionButton(
    icon: ImageVector,
    contentDescription: String,
    isVisible: Boolean,
    hasUnread: Boolean,
    onClick: () -> Unit,
    onMarkAsRead: () -> Unit,
    testTag: TestTag,
    modifier: Modifier = Modifier,
    dotAlignment: Alignment = Alignment.TopCenter,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible,
        enter = scaleIn(animationSpec = tween(220), initialScale = 0.8f) + fadeIn(animationSpec = tween(220)),
        exit = scaleOut(animationSpec = tween(180), targetScale = 0.8f) + fadeOut(animationSpec = tween(180)),
    ) {
        var menuExpanded by remember { mutableStateOf(false) }
        Box {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color = ElementTheme.colors.bgCanvasDefault, shape = CircleShape)
                    .clip(CircleShape)
                    .border(1.dp, ElementTheme.colors.borderDisabled, CircleShape)
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = { menuExpanded = true },
                        onLongClickLabel = stringResource(CommonStrings.action_open_context_menu),
                    )
                    .testTag(testTag),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = ElementTheme.colors.iconSecondary,
                )
                val menuTransitionState = remember { MutableTransitionState(false) }
                    .apply { targetState = menuExpanded }
                if (menuTransitionState.currentState || menuTransitionState.targetState) {
                    val gapPx = with(LocalDensity.current) { 8.dp.roundToPx() }
                    val positionProvider = remember(gapPx) { CenterStartOfAnchorPositionProvider(gapPx) }
                    Popup(
                        popupPositionProvider = positionProvider,
                        onDismissRequest = { menuExpanded = false },
                        properties = PopupProperties(focusable = true),
                    ) {
                        // Anchor the scale to the right-center edge so the menu visually grows
                        // outward from the FAB it's attached to.
                        val transformOrigin = TransformOrigin(pivotFractionX = 1f, pivotFractionY = 0.5f)
                        AnimatedVisibility(
                            visibleState = menuTransitionState,
                            enter = scaleIn(
                                animationSpec = tween(180),
                                initialScale = 0.9f,
                                transformOrigin = transformOrigin,
                            ) + fadeIn(animationSpec = tween(180)),
                            exit = scaleOut(
                                animationSpec = tween(140),
                                targetScale = 0.9f,
                                transformOrigin = transformOrigin,
                            ) + fadeOut(animationSpec = tween(140)),
                        ) {
                            // Hand-rolled instead of DropdownMenuItem: padding here is tighter
                            // than DropdownMenuItem's 12.dp default to match the Figma spec.
                            Row(
                                modifier = Modifier
                                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(8.dp))
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ElementTheme.colors.bgCanvasDefaultLevel1)
                                    .border(1.dp, ElementTheme.colors.borderDisabled, RoundedCornerShape(8.dp))
                                    .clickable {
                                        menuExpanded = false
                                        onMarkAsRead()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = CompoundIcons.MarkAsRead(),
                                    contentDescription = null,
                                    tint = ElementTheme.colors.iconTertiary,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(id = CommonStrings.action_mark_as_read),
                                    color = ElementTheme.colors.textPrimary,
                                    style = ElementTheme.typography.fontBodyLgRegular,
                                )
                            }
                        }
                    }
                }
            }
            val dotYOffset = if (dotAlignment == Alignment.BottomCenter) 4.dp else (-4).dp
            TimelineUnreadIndicator(
                isVisible = hasUnread,
                modifier = Modifier
                    .align(dotAlignment)
                    .offset { IntOffset(x = 0, y = dotYOffset.roundToPx()) },
            )
        }
    }
}

@Composable
private fun TimelineUnreadIndicator(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!isVisible) return
    Box(
        modifier = modifier
            .size(12.dp)
            .background(color = ElementTheme.colors.iconSuccessPrimary, shape = CircleShape)
            .border(width = 2.dp, color = ElementTheme.colors.bgCanvasDefault, shape = CircleShape),
    )
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
            onContentClick = {},
            onMessageLongClick = {},
            onSwipeToReply = {},
            onReactionClick = { _, _ -> },
            onReactionLongClick = { _, _ -> },
            onMoreReactionsClick = {},
            onReadReceiptClick = {},
            forceJumpToBottomVisibility = true,
        )
    }
}

@Composable
private fun TimelineViewWithReadMarker(
    hasUnreadAbove: Boolean,
    hasUnreadBelow: Boolean,
) {
    val timelineItems = persistentListOf<TimelineItem>(
        aTimelineItemEvent(isMine = false),
        aTimelineItemEvent(isMine = false),
        aTimelineItemEvent(isMine = true),
        aTimelineItemEvent(isMine = false),
        aTimelineItemEvent(isMine = false),
        aTimelineItemEvent(isMine = false),
    )
    CompositionLocalProvider(
        LocalTimelineItemPresenterFactories provides aFakeTimelineItemPresenterFactories(),
    ) {
        TimelineView(
            state = aTimelineState(
                timelineItems = timelineItems,
                displayJumpToUnread = true,
                // Index points past the loaded items, mirroring the real-world state the FAB
                // represents: the user has scrolled past the read marker, so it's no longer in
                // view. The actual scroll target doesn't matter for a static preview.
                readMarkerIndex = if (hasUnreadAbove) timelineItems.size else null,
                newEventState = if (hasUnreadBelow) NewEventState.FromOther else NewEventState.None,
            ),
            timelineProtectionState = aTimelineProtectionState(),
            onUserDataClick = {},
            onLinkClick = {},
            onContentClick = {},
            onMessageLongClick = {},
            onSwipeToReply = {},
            onReactionClick = { _, _ -> },
            onReactionLongClick = { _, _ -> },
            onMoreReactionsClick = {},
            onReadReceiptClick = {},
            forceJumpToBottomVisibility = true,
            forceJumpToReadMarkerVisibility = true,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineViewWithReadMarkerNoIndicatorsPreview() = ElementPreview {
    TimelineViewWithReadMarker(hasUnreadAbove = false, hasUnreadBelow = false)
}

@PreviewsDayNight
@Composable
internal fun TimelineViewWithReadMarkerBothIndicatorsPreview() = ElementPreview {
    TimelineViewWithReadMarker(hasUnreadAbove = true, hasUnreadBelow = true)
}

/**
 * Anchors the popup so its right edge sits [gapPx] to the left of the anchor and its vertical
 * center matches the anchor's. Adapts to localized menu widths and FAB size; coerced to stay
 * on-screen.
 */
private class CenterStartOfAnchorPositionProvider(
    private val gapPx: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val x = anchorBounds.left - popupContentSize.width - gapPx
        val y = anchorBounds.top + (anchorBounds.height - popupContentSize.height) / 2
        return IntOffset(
            x = x.coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0)),
            y = y.coerceIn(0, (windowSize.height - popupContentSize.height).coerceAtLeast(0)),
        )
    }
}
