/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.style.TextOverflow
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
import io.element.android.features.messages.impl.timeline.model.TimelineItemThreadInfo
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
import io.element.android.features.messages.impl.timeline.protection.mustBeProtected
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.colors.AvatarColorsProvider
import io.element.android.libraries.designsystem.components.EqualWidthColumn
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.modifiers.niceClickable
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.swipe.SwipeableActionsState
import io.element.android.libraries.designsystem.swipe.rememberSwipeableActionsState
import io.element.android.libraries.designsystem.text.toPx
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.toThreadId
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.EmbeddedEventInfo
import io.element.android.libraries.matrix.api.timeline.item.ThreadSummary
import io.element.android.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileDetails
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.getAvatarUrl
import io.element.android.libraries.matrix.api.timeline.item.event.getDisambiguatedDisplayName
import io.element.android.libraries.matrix.api.timeline.item.event.getDisplayName
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToDetails
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToView
import io.element.android.libraries.matrix.ui.messages.reply.eventId
import io.element.android.libraries.matrix.ui.messages.sender.SenderName
import io.element.android.libraries.matrix.ui.messages.sender.SenderNameMode
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.CommonPlurals
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.libraries.ui.utils.time.isTalkbackActive
import io.element.android.wysiwyg.link.Link
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
    timelineMode: Timeline.Mode,
    timelineRoomInfo: TimelineRoomInfo,
    timelineProtectionState: TimelineProtectionState,
    renderReadReceipts: Boolean,
    isLastOutgoingMessage: Boolean,
    displayThreadSummaries: Boolean,
    onEventClick: () -> Unit,
    onLongClick: () -> Unit,
    onLinkClick: (Link) -> Unit,
    onLinkLongClick: (Link) -> Unit,
    onUserDataClick: (MatrixUser) -> Unit,
    inReplyToClick: (EventId) -> Unit,
    onReactionClick: (emoji: String, eventId: TimelineItem.Event) -> Unit,
    onReactionLongClick: (emoji: String, eventId: TimelineItem.Event) -> Unit,
    onMoreReactionsClick: (eventId: TimelineItem.Event) -> Unit,
    onReadReceiptClick: (event: TimelineItem.Event) -> Unit,
    onSwipeToReply: () -> Unit,
    eventSink: (TimelineEvents.EventFromTimelineItem) -> Unit,
    modifier: Modifier = Modifier,
    eventContentView: @Composable (Modifier, (ContentAvoidingLayoutData) -> Unit) -> Unit = { contentModifier, onContentLayoutChange ->
        // Only pass down a custom clickable lambda if the content can be clicked separately
        val onContentClick = onEventClick.takeUnless { event.isWholeContentClickable }

        TimelineItemEventContentView(
            content = event.content,
            hideMediaContent = timelineProtectionState.hideMediaContent(event.eventId),
            onContentClick = onContentClick,
            onLongClick = onLongClick,
            onShowContentClick = { timelineProtectionState.eventSink(TimelineProtectionEvent.ShowContent(event.eventId)) },
            onLinkClick = onLinkClick,
            onLinkLongClick = onLinkLongClick,
            eventSink = eventSink,
            modifier = contentModifier,
            onContentLayoutChange = onContentLayoutChange
        )
    },
) {
    val coroutineScope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }

    val onContentClick = if (event.mustBeProtected()) {
        // In this case, let the content handle the click
        {}
    } else {
        onEventClick
    }

    fun onUserDataClick() {
        val sender = MatrixUser(
            userId = event.senderId,
            displayName = event.senderProfile.getDisplayName(),
            avatarUrl = event.senderProfile.getAvatarUrl(),
        )
        onUserDataClick(sender)
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
                        timelineMode = timelineMode,
                        timelineProtectionState = timelineProtectionState,
                        timelineRoomInfo = timelineRoomInfo,
                        interactionSource = interactionSource,
                        onContentClick = onContentClick,
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
                timelineMode = timelineMode,
                timelineProtectionState = timelineProtectionState,
                timelineRoomInfo = timelineRoomInfo,
                interactionSource = interactionSource,
                onContentClick = onContentClick,
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

        if (displayThreadSummaries && timelineMode !is Timeline.Mode.Thread && event.threadInfo is TimelineItemThreadInfo.ThreadRoot) {
            ThreadSummaryView(
                modifier = if (event.isMine) {
                    Modifier.align(Alignment.End).padding(end = 16.dp)
                } else {
                    if (timelineRoomInfo.isDm) Modifier else Modifier.padding(start = 16.dp)
                }.padding(top = 2.dp),
                threadSummary = event.threadInfo.summary,
                latestEventText = event.threadInfo.latestEventText,
                isOutgoing = event.isMine,
                onClick = {
                    event.eventId?.let {
                        eventSink(TimelineEvents.OpenThread(it.toThreadId(), null))
                    }
                }
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
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun ThreadSummaryView(
    threadSummary: ThreadSummary,
    latestEventText: String?,
    isOutgoing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        Row(
            modifier = Modifier
                .then(if (!isOutgoing) Modifier.padding(start = 16.dp) else Modifier)
                .graphicsLayer {
                    shape = RoundedCornerShape(8.dp)
                    clip = true
                }
                .background(MessageEventBubbleDefaults.backgroundBubbleColor(isOutgoing))
                .niceClickable(onClick)
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .widthIn(max = (maxWidth - 24.dp) * MessageEventBubbleDefaults.BUBBLE_WIDTH_RATIO),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = CompoundIcons.ThreadsSolid(),
                contentDescription = null,
                tint = ElementTheme.colors.iconSecondary,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = pluralStringResource(CommonPlurals.common_replies, threadSummary.numberOfReplies.toInt(), threadSummary.numberOfReplies),
                style = ElementTheme.typography.fontBodySmMedium,
                color = ElementTheme.colors.textSecondary,
            )

            Spacer(modifier = Modifier.width(8.dp))

            threadSummary.latestEvent.dataOrNull()?.let { latestEvent ->
                val avatarData = AvatarData(
                    id = latestEvent.senderId.value,
                    name = latestEvent.senderProfile.getDisplayName(),
                    url = latestEvent.senderProfile.getAvatarUrl(),
                    size = AvatarSize.TimelineThreadLatestEventSender,
                )
                Avatar(
                    avatarData = avatarData,
                    avatarType = AvatarType.User,
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = latestEvent.senderProfile.getDisambiguatedDisplayName(latestEvent.senderId),
                    style = ElementTheme.typography.fontBodySmMedium,
                    color = ElementTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.width(4.dp))

                latestEventText?.let {
                    Text(
                        text = it,
                        style = ElementTheme.typography.fontBodySmRegular,
                        color = ElementTheme.colors.textSecondary,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                }
            }
        }
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

@Composable
private fun TimelineItemEventRowContent(
    event: TimelineItem.Event,
    timelineMode: Timeline.Mode,
    timelineProtectionState: TimelineProtectionState,
    timelineRoomInfo: TimelineRoomInfo,
    interactionSource: MutableInteractionSource,
    onContentClick: () -> Unit,
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
                onUserDataClick,
                Modifier
                    .constrainAs(sender) {
                        top.linkTo(parent.top)
                        // Required for correct RTL layout
                        start.linkTo(parent.start)
                    }
                    .padding(horizontal = 16.dp)
                    .zIndex(1f),
            )
        }

        // Message bubble
        val bubbleState = BubbleState(
            groupPosition = event.groupPosition,
            isMine = event.isMine,
            timelineRoomInfo = timelineRoomInfo,
        )
        MessageEventBubble(
            modifier = Modifier
                .constrainAs(message) {
                    val topMargin = if (bubbleState.cutTopStart) {
                        NEGATIVE_MARGIN_FOR_BUBBLE
                    } else {
                        0.dp
                    }
                    top.linkTo(sender.bottom, margin = topMargin)
                    if (event.isMine) {
                        end.linkTo(parent.end, margin = 16.dp)
                    } else {
                        val startMargin = if (timelineRoomInfo.isDm) 16.dp else 16.dp + BUBBLE_INCOMING_OFFSET
                        start.linkTo(parent.start, margin = startMargin)
                    }
                },
            state = bubbleState,
            interactionSource = interactionSource,
            onClick = onContentClick,
            onLongClick = onLongClick,
        ) {
            MessageEventBubbleContent(
                event = event,
                timelineMode = timelineMode,
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
    senderProfile: ProfileDetails,
    senderAvatar: AvatarData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val avatarColors = AvatarColorsProvider.provide(senderAvatar.id)
    Row(
        modifier = modifier
            // Add external clickable modifier with no indicator so the touch target is larger than just the display name
            .clickable(onClick = onClick, enabled = true, interactionSource = remember { MutableInteractionSource() }, indication = null)
            .clearAndSetSemantics {
                hideFromAccessibility()
            }
    ) {
        Avatar(
            modifier = Modifier
                .testTag(TestTags.timelineItemSenderAvatar)
                .clip(CircleShape)
                .clickable(onClick = onClick),
            avatarData = senderAvatar,
            avatarType = AvatarType.User,
        )
        SenderName(
            modifier = Modifier
                .testTag(TestTags.timelineItemSenderName)
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 4.dp),
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
    timelineMode: Timeline.Mode,
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
        paddingBehaviour: ContentPadding,
        inReplyToDetails: InReplyToDetails?,
        modifier: Modifier = Modifier,
        canShrinkContent: Boolean = false,
    ) {
        val timestampLayoutModifier =
            if (inReplyToDetails != null && timestampPosition == TimestampPosition.Overlay) {
                Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            } else {
                Modifier
            }

        val topPadding = if (inReplyToDetails != null) 0.dp else 8.dp
        val contentModifier = when (paddingBehaviour) {
            ContentPadding.Textual ->
                Modifier.padding(start = 12.dp, end = 12.dp, top = topPadding, bottom = 8.dp)
            ContentPadding.Media -> {
                if (inReplyToDetails == null) {
                    Modifier
                } else {
                    Modifier.clip(RoundedCornerShape(10.dp))
                }
            }
            ContentPadding.CaptionedMedia ->
                Modifier.padding(start = 8.dp, end = 8.dp, top = topPadding, bottom = 8.dp)
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
                modifier = timestampLayoutModifier.semantics(mergeDescendants = false) {
                    isTraversalGroup = true
                    traversalIndex = -1f
                },
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

            val talkbackCompatModifier = if (isTalkbackActive()) {
                // Use z-index to make the replied to text being read after the message
                // Usually, you'd use traversalIndex for that, but it's not working for some reason
                inReplyToModifier.zIndex(1f)
            } else {
                inReplyToModifier.clickable(onClick = inReplyToClick)
            }
            InReplyToView(
                inReplyTo = inReplyTo,
                hideImage = timelineProtectionState.hideMediaContent(inReplyTo.eventId()),
                modifier = talkbackCompatModifier,
            )
        }
        if (inReplyToDetails != null) {
            // Use SubComposeLayout only if necessary as it can have consequences on the performance.
            EqualWidthColumn(spacing = 8.dp) {
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
    val paddingBehaviour = when (event.content) {
        is TimelineItemImageContent -> if (event.content.showCaption) ContentPadding.CaptionedMedia else ContentPadding.Media
        is TimelineItemVideoContent -> if (event.content.showCaption) ContentPadding.CaptionedMedia else ContentPadding.Media
        is TimelineItemStickerContent,
        is TimelineItemLocationContent -> ContentPadding.Media
        else -> ContentPadding.Textual
    }
    CommonLayout(
        showThreadDecoration = timelineMode !is Timeline.Mode.Thread && event.threadInfo is TimelineItemThreadInfo.ThreadResponse,
        timestampPosition = timestampPosition,
        paddingBehaviour = paddingBehaviour,
        inReplyToDetails = event.inReplyTo,
        canShrinkContent = event.content is TimelineItemVoiceContent,
        modifier = bubbleModifier,
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

@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowWithThreadSummaryPreview() = ElementPreview {
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
                    threadInfo = TimelineItemThreadInfo.ThreadRoot(
                        latestEventText = "This is the latest message in the thread",
                        summary = ThreadSummary(AsyncData.Success(
                            EmbeddedEventInfo(
                                eventOrTransactionId = EventOrTransactionId.Event(EventId("\$event-id")),
                                content = MessageContent(
                                    body = "This is the latest message in the thread",
                                    inReplyTo = null,
                                    isEdited = false,
                                    threadInfo = null,
                                    type = TextMessageType("This is the latest message in the thread", null)
                                ),
                                senderId = UserId("@user:id"),
                                senderProfile = ProfileDetails.Ready(
                                    displayName = "Alice",
                                    avatarUrl = null,
                                    displayNameAmbiguous = false,
                                ),
                                timestamp = 0L,
                            )
                        ), numberOfReplies = 20L)
                    )
                ),
                displayThreadSummaries = true,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun ThreadSummaryViewPreview() {
    ElementPreview {
        val body = "This is the latest message in the thread"
        val threadSummary = ThreadSummary(
            AsyncData.Success(
                EmbeddedEventInfo(
                    eventOrTransactionId = EventOrTransactionId.Event(EventId("\$event-id")),
                    content = MessageContent(
                        body = body,
                        inReplyTo = null,
                        isEdited = false,
                        threadInfo = null,
                        type = TextMessageType(body, null)
                    ),
                    senderId = UserId("@user:id"),
                    senderProfile = ProfileDetails.Ready(
                        displayName = "Alice",
                        avatarUrl = null,
                        displayNameAmbiguous = true,
                    ),
                    timestamp = 0L,
                )
            ),
            numberOfReplies = 12,
        )

        ThreadSummaryView(
            threadSummary = threadSummary,
            latestEventText = "Some event with a very long text that should get clipped",
            isOutgoing = true,
            onClick = {},
        )
    }
}
