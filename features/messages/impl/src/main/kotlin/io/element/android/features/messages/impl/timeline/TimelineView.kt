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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.impl.R
import io.element.android.features.messages.impl.timeline.components.TimelineItemEventRow
import io.element.android.features.messages.impl.timeline.components.TimelineItemStateEventRow
import io.element.android.features.messages.impl.timeline.components.TimelineItemVirtualRow
import io.element.android.features.messages.impl.timeline.components.group.GroupHeaderView
import io.element.android.features.messages.impl.timeline.components.virtual.TimelineLoadingMoreIndicator
import io.element.android.features.messages.impl.timeline.di.LocalTimelineItemPresenterFactories
import io.element.android.features.messages.impl.timeline.di.aFakeTimelineItemPresenterFactories
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContentProvider
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStateContent
import io.element.android.features.messages.impl.timeline.model.event.canBeRepliedTo
import io.element.android.features.messages.impl.timeline.session.SessionState
import io.element.android.libraries.designsystem.animation.alphaAnimation
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.composeutils.annotations.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.FloatingActionButton
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.theme.ElementTheme
import kotlinx.coroutines.launch

@Composable
fun TimelineView(
    state: TimelineState,
    onUserDataClicked: (UserId) -> Unit,
    onMessageClicked: (TimelineItem.Event) -> Unit,
    onMessageLongClicked: (TimelineItem.Event) -> Unit,
    onTimestampClicked: (TimelineItem.Event) -> Unit,
    onSwipeToReply: (TimelineItem.Event) -> Unit,
    onReactionClicked: (emoji: String, TimelineItem.Event) -> Unit,
    onReactionLongClicked: (emoji: String, TimelineItem.Event) -> Unit,
    onMoreReactionsClicked: (TimelineItem.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    fun onReachedLoadMore() {
        state.eventSink(TimelineEvents.LoadMore)
    }

    fun onScrollFinishedAt(firstVisibleIndex: Int) {
        state.eventSink(TimelineEvents.OnScrollFinished(firstVisibleIndex))
    }

    val lazyListState = rememberLazyListState()

    @Suppress("UNUSED_PARAMETER")
    fun inReplyToClicked(eventId: EventId) {
        // TODO implement this logic once we have support to 'jump to event X' in sliding sync
    }

    // Animate alpha when timeline is first displayed, to avoid flashes or glitching when viewing rooms
    val alpha by alphaAnimation(label = "alpha for timeline")

    Box(modifier = modifier.alpha(alpha)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            reverseLayout = true,
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            items(
                items = state.timelineItems,
                contentType = { timelineItem -> timelineItem.contentType() },
                key = { timelineItem -> timelineItem.identifier() },
            ) { timelineItem ->
                TimelineItemRow(
                    timelineItem = timelineItem,
                    highlightedItem = state.highlightedEventId?.value,
                    userHasPermissionToSendMessage = state.userHasPermissionToSendMessage,
                    onClick = onMessageClicked,
                    onLongClick = onMessageLongClicked,
                    onUserDataClick = onUserDataClicked,
                    inReplyToClick = ::inReplyToClicked,
                    onReactionClick = onReactionClicked,
                    onReactionLongClick = onReactionLongClicked,
                    onMoreReactionsClick = onMoreReactionsClicked,
                    onTimestampClicked = onTimestampClicked,
                    sessionState = state.sessionState,
                    eventSink = state.eventSink,
                    onSwipeToReply = onSwipeToReply,
                )
            }
            if (state.paginationState.hasMoreToLoadBackwards) {
                // Do not use key parameter to avoid wrong positioning
                item(contentType = "TimelineLoadingMoreIndicator") {
                    TimelineLoadingMoreIndicator()
                    LaunchedEffect(Unit) {
                        onReachedLoadMore()
                    }
                }
            }
        }

        TimelineScrollHelper(
            isTimelineEmpty = state.timelineItems.isEmpty(),
            lazyListState = lazyListState,
            hasNewItems = state.hasNewItems,
            onScrollFinishedAt = ::onScrollFinishedAt
        )
    }
}

@Composable
private fun TimelineItemRow(
    timelineItem: TimelineItem,
    highlightedItem: String?,
    userHasPermissionToSendMessage: Boolean,
    sessionState: SessionState,
    onUserDataClick: (UserId) -> Unit,
    onClick: (TimelineItem.Event) -> Unit,
    onLongClick: (TimelineItem.Event) -> Unit,
    inReplyToClick: (EventId) -> Unit,
    onReactionClick: (key: String, TimelineItem.Event) -> Unit,
    onReactionLongClick: (key: String, TimelineItem.Event) -> Unit,
    onMoreReactionsClick: (TimelineItem.Event) -> Unit,
    onTimestampClicked: (TimelineItem.Event) -> Unit,
    onSwipeToReply: (TimelineItem.Event) -> Unit,
    eventSink: (TimelineEvents) -> Unit,
    modifier: Modifier = Modifier
) {
    when (timelineItem) {
        is TimelineItem.Virtual -> {
            TimelineItemVirtualRow(
                virtual = timelineItem,
                sessionState = sessionState,
                modifier = modifier,
            )
        }
        is TimelineItem.Event -> {
            if (timelineItem.content is TimelineItemStateContent) {
                TimelineItemStateEventRow(
                    event = timelineItem,
                    isHighlighted = highlightedItem == timelineItem.identifier(),
                    onClick = { onClick(timelineItem) },
                    onLongClick = { onLongClick(timelineItem) },
                    eventSink = eventSink,
                    modifier = modifier,
                )
            } else {
                TimelineItemEventRow(
                    event = timelineItem,
                    isHighlighted = highlightedItem == timelineItem.identifier(),
                    canReply = userHasPermissionToSendMessage && timelineItem.content.canBeRepliedTo(),
                    onClick = { onClick(timelineItem) },
                    onLongClick = { onLongClick(timelineItem) },
                    onUserDataClick = onUserDataClick,
                    inReplyToClick = inReplyToClick,
                    onReactionClick = onReactionClick,
                    onReactionLongClick = onReactionLongClick,
                    onMoreReactionsClick = onMoreReactionsClick,
                    onTimestampClicked = onTimestampClicked,
                    onSwipeToReply = { onSwipeToReply(timelineItem) },
                    eventSink = eventSink,
                    modifier = modifier,
                )
            }
        }
        is TimelineItem.GroupedEvents -> {
            val isExpanded = rememberSaveable(key = timelineItem.identifier()) { mutableStateOf(false) }

            fun onExpandGroupClick() {
                isExpanded.value = !isExpanded.value
            }

            Column(modifier = modifier.animateContentSize()) {
                GroupHeaderView(
                    text = pluralStringResource(
                        id = R.plurals.room_timeline_state_changes,
                        count = timelineItem.events.size,
                        timelineItem.events.size
                    ),
                    isExpanded = isExpanded.value,
                    isHighlighted = !isExpanded.value && timelineItem.events.any { it.identifier() == highlightedItem },
                    onClick = ::onExpandGroupClick,
                )
                if (isExpanded.value) {
                    Column {
                        timelineItem.events.forEach { subGroupEvent ->
                            TimelineItemRow(
                                timelineItem = subGroupEvent,
                                highlightedItem = highlightedItem,
                                sessionState = sessionState,
                                userHasPermissionToSendMessage = false,
                                onClick = onClick,
                                onLongClick = onLongClick,
                                inReplyToClick = inReplyToClick,
                                onUserDataClick = onUserDataClick,
                                onTimestampClicked = onTimestampClicked,
                                onReactionClick = onReactionClick,
                                onReactionLongClick = onReactionLongClick,
                                onMoreReactionsClick = onMoreReactionsClick,
                                eventSink = eventSink,
                                onSwipeToReply = {},
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.TimelineScrollHelper(
    isTimelineEmpty: Boolean,
    lazyListState: LazyListState,
    hasNewItems: Boolean,
    onScrollFinishedAt: (Int) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val isScrollFinished by remember { derivedStateOf { !lazyListState.isScrollInProgress } }
    val canAutoScroll by remember { derivedStateOf { lazyListState.firstVisibleItemIndex < 3 } }

    LaunchedEffect(canAutoScroll, hasNewItems) {
        val shouldAutoScroll = isScrollFinished && canAutoScroll && hasNewItems
        if (shouldAutoScroll) {
            coroutineScope.launch {
                lazyListState.animateScrollToItem(0)
            }
        }
    }

    LaunchedEffect(isScrollFinished, isTimelineEmpty) {
        if (isScrollFinished && !isTimelineEmpty) {
            // Notify the parent composable about the first visible item index when scrolling finishes
            onScrollFinishedAt(lazyListState.firstVisibleItemIndex)
        }
    }

    JumpToBottomButton(
        // Use inverse of canAutoScroll otherwise we might briefly see the before the scroll animation is triggered
        isVisible = !canAutoScroll,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 24.dp, bottom = 12.dp),
        onClick = {
            coroutineScope.launch {
                if (lazyListState.firstVisibleItemIndex > 10) {
                    lazyListState.scrollToItem(0)
                } else {
                    lazyListState.animateScrollToItem(0)
                }
            }
        }
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
        visible = isVisible || LocalInspectionMode.current,
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
                resourceId = CommonDrawables.ic_compound_arrow_right,
                contentDescription = "",
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
            onMessageClicked = {},
            onTimestampClicked = {},
            onUserDataClicked = {},
            onMessageLongClicked = {},
            onReactionClicked = { _, _ -> },
            onReactionLongClicked = { _, _ -> },
            onMoreReactionsClicked = {},
            onSwipeToReply = {},
        )
    }
}
