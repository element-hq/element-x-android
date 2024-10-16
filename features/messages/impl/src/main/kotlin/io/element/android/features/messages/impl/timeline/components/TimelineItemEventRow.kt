/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstrainScope
import androidx.constraintlayout.compose.ConstraintLayout
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.timeline.TimelineEvents
import io.element.android.features.messages.impl.timeline.TimelineRoomInfo
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.components.event.TimelineItemEventContentView
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayout
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.components.receipt.ReadReceiptViewState
import io.element.android.features.messages.impl.timeline.components.receipt.TimelineItemReadReceiptView
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition
import io.element.android.features.messages.impl.timeline.model.bubble.BubbleState
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemPollContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStickerContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.features.messages.impl.timeline.protection.TimelineProtectionEvent
import io.element.android.features.messages.impl.timeline.protection.TimelineProtectionState
import io.element.android.libraries.designsystem.colors.AvatarColorsProvider
import io.element.android.libraries.designsystem.components.EqualWidthColumn
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.swipe.SwipeableActionsState
import io.element.android.libraries.designsystem.swipe.rememberSwipeableActionsState
import io.element.android.libraries.designsystem.text.toPx
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileTimelineDetails
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToDetails
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToView
import io.element.android.libraries.matrix.ui.messages.reply.eventId
import io.element.android.libraries.matrix.ui.messages.sender.SenderName
import io.element.android.libraries.matrix.ui.messages.sender.SenderNameMode
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

// The bubble has a negative margin to be placed a bit upper regarding the sender
// information and overlap the avatar.
val NEGATIVE_MARGIN_FOR_BUBBLE = (-8).dp

// Width of the transparent border around the sender avatar
val SENDER_AVATAR_BORDER_WIDTH = 3.dp

private val BUBBLE_INCOMING_OFFSET = 16.dp

@Composable
fun TimelineItemEventRow(
    event: TimelineItem.Event,
    timelineRoomInfo: TimelineRoomInfo,
    timelineProtectionState: TimelineProtectionState,
    renderReadReceipts: Boolean,
    isLastOutgoingMessage: Boolean,
    isHighlighted: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onLinkClick: (String) -> Unit,
    onUserDataClick: (UserId) -> Unit,
    inReplyToClick: (EventId) -> Unit,
    onReactionClick: (emoji: String, eventId: TimelineItem.Event) -> Unit,
    onReactionLongClick: (emoji: String, eventId: TimelineItem.Event) -> Unit,
    onMoreReactionsClick: (eventId: TimelineItem.Event) -> Unit,
    onReadReceiptClick: (event: TimelineItem.Event) -> Unit,
    onSwipeToReply: () -> Unit,
    eventSink: (TimelineEvents.EventFromTimelineItem) -> Unit,
    modifier: Modifier = Modifier,
    eventContentView: @Composable (Modifier, (ContentAvoidingLayoutData) -> Unit) -> Unit = { contentModifier, onContentLayoutChange ->
        TimelineItemEventContentView(
            content = event.content,
            hideMediaContent = timelineProtectionState.hideMediaContent(event.eventId),
            onShowClick = { timelineProtectionState.eventSink(TimelineProtectionEvent.ShowContent(event.eventId)) },
            onLinkClick = onLinkClick,
            eventSink = eventSink,
            modifier = contentModifier,
            onContentLayoutChange = onContentLayoutChange
        )
    },
) {
    val coroutineScope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }

    fun onUserDataClick() {
        onUserDataClick(event.senderId)
    }

    fun inReplyToClick() {
        val inReplyToEventId = event.inReplyTo?.eventId() ?: return
        inReplyToClick(inReplyToEventId)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (event.groupPosition.isNew()) {
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Spacer(modifier = Modifier.height(2.dp))
        }
        val canReply = timelineRoomInfo.userHasPermissionToSendMessage && event.canBeRepliedTo
        if (canReply) {
            val state: SwipeableActionsState = rememberSwipeableActionsState()
            val offset = state.offset.floatValue
            val swipeThresholdPx = 40.dp.toPx()
            val thresholdCrossed = abs(offset) > swipeThresholdPx
            SwipeSensitivity(3f) {
                Box(Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.matchParentSize()) {
                        ReplySwipeIndicator({ offset / 120 })
                    }
                    TimelineItemEventRowContent(
                        event = event,
                        timelineProtectionState = timelineProtectionState,
                        isHighlighted = isHighlighted,
                        timelineRoomInfo = timelineRoomInfo,
                        interactionSource = interactionSource,
                        onClick = onClick,
                        onLongClick = onLongClick,
                        inReplyToClick = ::inReplyToClick,
                        onUserDataClick = ::onUserDataClick,
                        onReactionClick = { emoji -> onReactionClick(emoji, event) },
                        onReactionLongClick = { emoji -> onReactionLongClick(emoji, event) },
                        onMoreReactionsClick = { onMoreReactionsClick(event) },
                        modifier = Modifier
                            .absoluteOffset { IntOffset(x = offset.roundToInt(), y = 0) }
                            .draggable(
                                orientation = Orientation.Horizontal,
                                enabled = !state.isResettingOnRelease,
                                onDragStopped = {
                                    coroutineScope.launch {
                                        if (thresholdCrossed) {
                                            onSwipeToReply()
                                        }
                                        state.resetOffset()
                                    }
                                },
                                state = state.draggableState,
                            ),
                        eventSink = eventSink,
                        eventContentView = eventContentView,
                    )
                }
            }
        } else {
            TimelineItemEventRowContent(
                event = event,
                timelineProtectionState = timelineProtectionState,
                isHighlighted = isHighlighted,
                timelineRoomInfo = timelineRoomInfo,
                interactionSource = interactionSource,
                onClick = onClick,
                onLongClick = onLongClick,
                inReplyToClick = ::inReplyToClick,
                onUserDataClick = ::onUserDataClick,
                onReactionClick = { emoji -> onReactionClick(emoji, event) },
                onReactionLongClick = { emoji -> onReactionLongClick(emoji, event) },
                onMoreReactionsClick = { onMoreReactionsClick(event) },
                eventSink = eventSink,
                eventContentView = eventContentView,
            )
        }
        // Read receipts / Send state
        TimelineItemReadReceiptView(
            state = ReadReceiptViewState(
                sendState = event.localSendState,
                isLastOutgoingMessage = isLastOutgoingMessage,
                receipts = event.readReceiptState.receipts,
            ),
            renderReadReceipts = renderReadReceipts,
            onReadReceiptsClick = { onReadReceiptClick(event) },
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

/**
 * Impact ViewConfiguration.touchSlop by [sensitivityFactor].
 * Inspired from https://issuetracker.google.com/u/1/issues/269627294.
 * @param sensitivityFactor the factor to multiply the touchSlop by. The highest value, the more the user will
 * have to drag to start the drag.
 * @param content the content to display.
 */
@Composable
private fun SwipeSensitivity(
    sensitivityFactor: Float,
    content: @Composable () -> Unit,
) {
    val current = LocalViewConfiguration.current
    CompositionLocalProvider(
        LocalViewConfiguration provides object : ViewConfiguration by current {
            override val touchSlop: Float
                get() = current.touchSlop * sensitivityFactor
        }
    ) {
        content()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TimelineItemEventRowContent(
    event: TimelineItem.Event,
    timelineProtectionState: TimelineProtectionState,
    isHighlighted: Boolean,
    timelineRoomInfo: TimelineRoomInfo,
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    inReplyToClick: () -> Unit,
    onUserDataClick: () -> Unit,
    onReactionClick: (emoji: String) -> Unit,
    onReactionLongClick: (emoji: String) -> Unit,
    onMoreReactionsClick: (event: TimelineItem.Event) -> Unit,
    eventSink: (TimelineEvents.EventFromTimelineItem) -> Unit,
    modifier: Modifier = Modifier,
    eventContentView: @Composable (Modifier, (ContentAvoidingLayoutData) -> Unit) -> Unit,
) {
    fun ConstrainScope.linkStartOrEnd(event: TimelineItem.Event) = if (event.isMine) {
        end.linkTo(parent.end)
    } else {
        start.linkTo(parent.start)
    }

    ConstraintLayout(
        modifier = modifier
            .wrapContentHeight()
            .fillMaxWidth(),
    ) {
        val (
            sender,
            message,
            reactions,
            pinIcon,
        ) = createRefs()

        // Sender
        if (event.showSenderInformation && !timelineRoomInfo.isDm) {
            MessageSenderInformation(
                event.senderId,
                event.senderProfile,
                event.senderAvatar,
                Modifier
                    .constrainAs(sender) {
                        top.linkTo(parent.top)
                        // Required for correct RTL layout
                        start.linkTo(parent.start)
                    }
                    .padding(horizontal = 16.dp)
                    .zIndex(1f)
                    .clickable(onClick = onUserDataClick)
                    // This is redundant when using talkback
                    .clearAndSetSemantics {
                        invisibleToUser()
                        testTag = TestTags.timelineItemSenderInfo.value
                    }
            )
        }

        // Message bubble
        val bubbleState = BubbleState(
            groupPosition = event.groupPosition,
            isMine = event.isMine,
            isHighlighted = isHighlighted,
            timelineRoomInfo = timelineRoomInfo,
        )
        MessageEventBubble(
            modifier = Modifier
                .constrainAs(message) {
                    top.linkTo(sender.bottom, margin = NEGATIVE_MARGIN_FOR_BUBBLE)
                    if (event.isMine) {
                        end.linkTo(parent.end, margin = 16.dp)
                    } else {
                        val startMargin = if (timelineRoomInfo.isDm) 16.dp else 16.dp + BUBBLE_INCOMING_OFFSET
                        start.linkTo(parent.start, margin = startMargin)
                    }
                },
            state = bubbleState,
            interactionSource = interactionSource,
            onClick = onClick,
            onLongClick = onLongClick,
        ) {
            MessageEventBubbleContent(
                event = event,
                timelineProtectionState = timelineProtectionState,
                onMessageLongClick = onLongClick,
                inReplyToClick = inReplyToClick,
                eventSink = eventSink,
                eventContentView = eventContentView,
            )
        }

        // Pin icon
        val isEventPinned = timelineRoomInfo.pinnedEventIds.contains(event.eventId)
        if (isEventPinned) {
            Icon(
                imageVector = CompoundIcons.PinSolid(),
                contentDescription = stringResource(CommonStrings.common_pinned),
                tint = ElementTheme.colors.iconTertiary,
                modifier = Modifier
                    .padding(1.dp)
                    .size(16.dp)
                    .constrainAs(pinIcon) {
                        top.linkTo(message.top)
                        if (event.isMine) {
                            end.linkTo(message.start, margin = 8.dp)
                        } else {
                            start.linkTo(message.end, margin = 8.dp)
                        }
                    }
            )
        }

        // Reactions
        if (event.reactionsState.reactions.isNotEmpty()) {
            TimelineItemReactionsView(
                reactionsState = event.reactionsState,
                userCanSendReaction = timelineRoomInfo.userHasPermissionToSendReaction,
                isOutgoing = event.isMine,
                onReactionClick = onReactionClick,
                onReactionLongClick = onReactionLongClick,
                onMoreReactionsClick = { onMoreReactionsClick(event) },
                modifier = Modifier
                    .constrainAs(reactions) {
                        top.linkTo(message.bottom, margin = (-4).dp)
                        linkStartOrEnd(event)
                    }
                    .zIndex(1f)
                    .padding(
                        // Note: due to the applied constraints, start is left for other's message and right for mine
                        // In design we want a offset of 6.dp compare to the bubble, so start is 22.dp (16 + 6)
                        start = when {
                            event.isMine -> 22.dp
                            timelineRoomInfo.isDm -> 22.dp
                            else -> 22.dp + BUBBLE_INCOMING_OFFSET
                        },
                        end = 16.dp
                    )
            )
        }
    }
}

@Composable
private fun MessageSenderInformation(
    senderId: UserId,
    senderProfile: ProfileTimelineDetails,
    senderAvatar: AvatarData,
    modifier: Modifier = Modifier
) {
    val avatarColors = AvatarColorsProvider.provide(senderAvatar.id)
    Row(modifier = modifier) {
        Avatar(senderAvatar)
        Spacer(modifier = Modifier.width(4.dp))
        SenderName(
            senderId = senderId,
            senderProfile = senderProfile,
            senderNameMode = SenderNameMode.Timeline(avatarColors.foreground),
        )
    }
}

@Suppress("MultipleEmitters") // False positive
@Composable
private fun MessageEventBubbleContent(
    event: TimelineItem.Event,
    timelineProtectionState: TimelineProtectionState,
    onMessageLongClick: () -> Unit,
    inReplyToClick: () -> Unit,
    eventSink: (TimelineEvents.EventFromTimelineItem) -> Unit,
    @SuppressLint("ModifierParameter")
    // need to rename this modifier to prevent linter false positives
    @Suppress("ModifierNaming")
    bubbleModifier: Modifier = Modifier,
    eventContentView: @Composable (Modifier, (ContentAvoidingLayoutData) -> Unit) -> Unit,
) {
    // Long clicks are not not automatically propagated from a `clickable`
    // to its `combinedClickable` parent so we do it manually
    fun onTimestampLongClick() = onMessageLongClick()

    @Composable
    fun ThreadDecoration(
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.height(14.dp),
                imageVector = CompoundIcons.Threads(),
                contentDescription = null,
                tint = ElementTheme.colors.iconSecondary,
            )
            Text(
                text = stringResource(CommonStrings.common_thread),
                style = ElementTheme.typography.fontBodyXsRegular,
                color = ElementTheme.colors.textPrimary,
                modifier = Modifier.clearAndSetSemantics { }
            )
        }
    }

    @Composable
    fun WithTimestampLayout(
        timestampPosition: TimestampPosition,
        eventSink: (TimelineEvents.EventFromTimelineItem) -> Unit,
        modifier: Modifier = Modifier,
        canShrinkContent: Boolean = false,
        content: @Composable (onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit) -> Unit,
    ) {
        @Suppress("NAME_SHADOWING")
        val content = remember { movableContentOf(content) }
        when (timestampPosition) {
            TimestampPosition.Overlay ->
                Box(modifier, contentAlignment = Alignment.Center) {
                    content {}
                    TimelineEventTimestampView(
                        event = event,
                        eventSink = eventSink,
                        modifier = Modifier
                            // Outer padding
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .background(ElementTheme.colors.bgSubtleSecondary, RoundedCornerShape(10.0.dp))
                            .align(Alignment.BottomEnd)
                            // Inner padding
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            TimestampPosition.Aligned ->
                ContentAvoidingLayout(
                    modifier = modifier,
                    // The spacing is negative to make the content overlap the empty space at the start of the timestamp
                    spacing = (-4).dp,
                    overlayOffset = DpOffset(0.dp, -1.dp),
                    shrinkContent = canShrinkContent,
                    content = { content(this::onContentLayoutChange) },
                    overlay = {
                        TimelineEventTimestampView(
                            event = event,
                            eventSink = eventSink,
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                )
            TimestampPosition.Below ->
                Column(modifier) {
                    content {}
                    TimelineEventTimestampView(
                        event = event,
                        eventSink = eventSink,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
        }
    }

    /** Groups the different components in a Column with some space between them. */
    @Composable
    fun CommonLayout(
        timestampPosition: TimestampPosition,
        showThreadDecoration: Boolean,
        inReplyToDetails: InReplyToDetails?,
        modifier: Modifier = Modifier,
        canShrinkContent: Boolean = false,
    ) {
        val timestampLayoutModifier: Modifier
        val contentModifier: Modifier
        when {
            inReplyToDetails != null -> {
                if (timestampPosition == TimestampPosition.Overlay) {
                    timestampLayoutModifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                    contentModifier = Modifier.clip(RoundedCornerShape(12.dp))
                } else {
                    contentModifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 0.dp, bottom = 8.dp)
                    timestampLayoutModifier = Modifier
                }
            }
            timestampPosition != TimestampPosition.Overlay -> {
                timestampLayoutModifier = Modifier
                contentModifier = Modifier
                    .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
            }
            else -> {
                timestampLayoutModifier = Modifier
                contentModifier = Modifier
            }
        }
        val threadDecoration = @Composable {
            if (showThreadDecoration) {
                ThreadDecoration(modifier = Modifier.padding(top = 8.dp, start = 12.dp, end = 12.dp))
            }
        }
        val contentWithTimestamp = @Composable {
            WithTimestampLayout(
                timestampPosition = timestampPosition,
                eventSink = eventSink,
                canShrinkContent = canShrinkContent,
                modifier = timestampLayoutModifier,
                content = { onContentLayoutChange ->
                    eventContentView(contentModifier, onContentLayoutChange)
                }
            )
        }

        val inReplyTo = @Composable { inReplyTo: InReplyToDetails ->
            val topPadding = if (showThreadDecoration) 0.dp else 8.dp
            val inReplyToModifier = Modifier
                .padding(top = topPadding, start = 8.dp, end = 8.dp)
                .clip(RoundedCornerShape(6.dp))
                // FIXME when a node is clickable, its contents won't be added to the semantics tree of its parent
                .clickable(onClick = inReplyToClick)
            InReplyToView(
                inReplyTo = inReplyTo,
                hideImage = timelineProtectionState.hideMediaContent(inReplyTo.eventId()),
                modifier = inReplyToModifier,
            )
        }
        if (inReplyToDetails != null) {
            // Use SubComposeLayout only if necessary as it can have consequences on the performance.
            EqualWidthColumn(modifier = modifier, spacing = 8.dp) {
                threadDecoration()
                inReplyTo(inReplyToDetails)
                contentWithTimestamp()
            }
        } else {
            Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                threadDecoration()
                contentWithTimestamp()
            }
        }
    }

    val timestampPosition = when (event.content) {
        is TimelineItemImageContent -> if (event.content.showCaption) TimestampPosition.Aligned else TimestampPosition.Overlay
        is TimelineItemVideoContent -> if (event.content.showCaption) TimestampPosition.Aligned else TimestampPosition.Overlay
        is TimelineItemStickerContent,
        is TimelineItemLocationContent -> TimestampPosition.Overlay
        is TimelineItemPollContent -> TimestampPosition.Below
        else -> TimestampPosition.Default
    }
    CommonLayout(
        showThreadDecoration = event.isThreaded,
        timestampPosition = timestampPosition,
        inReplyToDetails = event.inReplyTo,
        canShrinkContent = event.content is TimelineItemVoiceContent,
        modifier = bubbleModifier.semantics(mergeDescendants = true) {
            contentDescription = event.safeSenderName
        }
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowPreview() = ElementPreview {
    Column {
        sequenceOf(false, true).forEach { isMine ->
            ATimelineItemEventRow(
                event = aTimelineItemEvent(
                    senderDisplayName = "Sender with a super long name that should ellipsize",
                    isMine = isMine,
                    content = aTimelineItemTextContent(
                        body = "A long text which will be displayed on several lines and" +
                            " hopefully can be manually adjusted to test different behaviors."
                    ),
                    groupPosition = TimelineItemGroupPosition.First,
                ),
            )
            ATimelineItemEventRow(
                event = aTimelineItemEvent(
                    isMine = isMine,
                    content = aTimelineItemImageContent(
                        aspectRatio = 2.5f
                    ),
                    groupPosition = TimelineItemGroupPosition.Last,
                ),
            )
        }
    }
}
