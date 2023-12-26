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

package io.element.android.features.messages.impl.timeline.components

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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
import io.element.android.features.messages.impl.timeline.model.InReplyToDetails
import io.element.android.features.messages.impl.timeline.model.InReplyToMetadata
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
import io.element.android.features.messages.impl.timeline.model.event.canBeRepliedTo
import io.element.android.features.messages.impl.timeline.model.metadata
import io.element.android.libraries.androidutils.system.openUrlInExternalApp
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
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import io.element.android.libraries.matrix.api.room.Mention
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnail
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun TimelineItemEventRow(
    event: TimelineItem.Event,
    timelineRoomInfo: TimelineRoomInfo,
    showReadReceipts: Boolean,
    isLastOutgoingMessage: Boolean,
    isHighlighted: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onUserDataClick: (UserId) -> Unit,
    inReplyToClick: (EventId) -> Unit,
    onTimestampClicked: (TimelineItem.Event) -> Unit,
    onReactionClick: (emoji: String, eventId: TimelineItem.Event) -> Unit,
    onReactionLongClick: (emoji: String, eventId: TimelineItem.Event) -> Unit,
    onMoreReactionsClick: (eventId: TimelineItem.Event) -> Unit,
    onReadReceiptClick: (event: TimelineItem.Event) -> Unit,
    onSwipeToReply: () -> Unit,
    eventSink: (TimelineEvents) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }

    fun onUserDataClicked() {
        onUserDataClick(event.senderId)
    }

    fun inReplyToClicked() {
        val inReplyToEventId = event.inReplyTo?.eventId ?: return
        inReplyToClick(inReplyToEventId)
    }

    fun onMentionClicked(mention: Mention) {
        when (mention) {
            is Mention.User -> onUserDataClick(mention.userId)
            else -> Unit // TODO implement actions for other mentions being clicked
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (event.groupPosition.isNew()) {
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Spacer(modifier = Modifier.height(2.dp))
        }
        val canReply = timelineRoomInfo.userHasPermissionToSendMessage && event.content.canBeRepliedTo()
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
                        event = event,
                        isHighlighted = isHighlighted,
                        timelineRoomInfo = timelineRoomInfo,
                        interactionSource = interactionSource,
                        onClick = onClick,
                        onLongClick = onLongClick,
                        onTimestampClicked = onTimestampClicked,
                        inReplyToClicked = ::inReplyToClicked,
                        onUserDataClicked = ::onUserDataClicked,
                        onReactionClicked = { emoji -> onReactionClick(emoji, event) },
                        onReactionLongClicked = { emoji -> onReactionLongClick(emoji, event) },
                        onMoreReactionsClicked = { onMoreReactionsClick(event) },
                        onMentionClicked = ::onMentionClicked,
                        eventSink = eventSink,
                    )
                }
            }
        } else {
            TimelineItemEventRowContent(
                event = event,
                isHighlighted = isHighlighted,
                timelineRoomInfo = timelineRoomInfo,
                interactionSource = interactionSource,
                onClick = onClick,
                onLongClick = onLongClick,
                onTimestampClicked = onTimestampClicked,
                inReplyToClicked = ::inReplyToClicked,
                onUserDataClicked = ::onUserDataClicked,
                onReactionClicked = { emoji -> onReactionClick(emoji, event) },
                onReactionLongClicked = { emoji -> onReactionLongClick(emoji, event) },
                onMoreReactionsClicked = { onMoreReactionsClick(event) },
                onMentionClicked = ::onMentionClicked,
                eventSink = eventSink,
            )
        }
        // Read receipts / Send state
        TimelineItemReadReceiptView(
            state = ReadReceiptViewState(
                sendState = event.localSendState,
                isLastOutgoingMessage = isLastOutgoingMessage,
                receipts = event.readReceiptState.receipts,
            ),
            showReadReceipts = showReadReceipts,
            onReadReceiptsClicked = { onReadReceiptClick(event) },
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

@Composable
private fun TimelineItemEventRowContent(
    event: TimelineItem.Event,
    isHighlighted: Boolean,
    timelineRoomInfo: TimelineRoomInfo,
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onTimestampClicked: (TimelineItem.Event) -> Unit,
    inReplyToClicked: () -> Unit,
    onUserDataClicked: () -> Unit,
    onReactionClicked: (emoji: String) -> Unit,
    onReactionLongClicked: (emoji: String) -> Unit,
    onMoreReactionsClicked: (event: TimelineItem.Event) -> Unit,
    onMentionClicked: (Mention) -> Unit,
    eventSink: (TimelineEvents) -> Unit,
    modifier: Modifier = Modifier,
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
        ) = createRefs()

        // Sender
        val avatarStrokeSize = 3.dp
        if (event.showSenderInformation && !timelineRoomInfo.isDirect) {
            MessageSenderInformation(
                event.safeSenderName,
                event.senderAvatar,
                avatarStrokeSize,
                Modifier
                    .constrainAs(sender) {
                        top.linkTo(parent.top)
                    }
                    .padding(horizontal = 16.dp)
                    .zIndex(1f)
                    .clickable(onClick = onUserDataClicked)
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
                    top.linkTo(sender.bottom, margin = -avatarStrokeSize - 8.dp)
                    this.linkStartOrEnd(event)
                },
            state = bubbleState,
            interactionSource = interactionSource,
            onClick = onClick,
            onLongClick = onLongClick,
        ) {
            MessageEventBubbleContent(
                event = event,
                onMessageLongClick = onLongClick,
                inReplyToClick = inReplyToClicked,
                onTimestampClicked = {
                    onTimestampClicked(event)
                },
                onMentionClicked = onMentionClicked,
                eventSink = eventSink,
            )
        }

        // Reactions
        if (event.reactionsState.reactions.isNotEmpty()) {
            TimelineItemReactionsView(
                reactionsState = event.reactionsState,
                userCanSendReaction = timelineRoomInfo.userHasPermissionToSendReaction,
                isOutgoing = event.isMine,
                onReactionClicked = onReactionClicked,
                onReactionLongClicked = onReactionLongClicked,
                onMoreReactionsClicked = { onMoreReactionsClicked(event) },
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
                            timelineRoomInfo.isDirect -> 22.dp
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
    sender: String,
    senderAvatar: AvatarData,
    avatarStrokeSize: Dp,
    modifier: Modifier = Modifier
) {
    val avatarStrokeColor = MaterialTheme.colorScheme.background
    val avatarSize = senderAvatar.size.dp
    val avatarColors = AvatarColorsProvider.provide(senderAvatar.id, ElementTheme.isLightTheme)
    Box(
        modifier = modifier
    ) {
        // Background of Avatar, to erase the corner of the message content
        Canvas(
            modifier = Modifier
                .size(size = avatarSize + avatarStrokeSize)
                .clipToBounds()
        ) {
            drawCircle(
                color = avatarStrokeColor,
                center = Offset(x = (avatarSize / 2).toPx(), y = (avatarSize / 2).toPx()),
                radius = (avatarSize / 2 + avatarStrokeSize).toPx()
            )
        }
        // Content
        Row {
            Avatar(senderAvatar)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                modifier = Modifier.clipToBounds(),
                text = sender,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = avatarColors.foreground,
                style = ElementTheme.typography.fontBodyMdMedium,
            )
        }
    }
}

@Composable
private fun MessageEventBubbleContent(
    event: TimelineItem.Event,
    onMessageLongClick: () -> Unit,
    inReplyToClick: () -> Unit,
    onTimestampClicked: () -> Unit,
    onMentionClicked: (Mention) -> Unit,
    eventSink: (TimelineEvents) -> Unit,
    @SuppressLint("ModifierParameter")
    @Suppress("ModifierNaming")
    bubbleModifier: Modifier = Modifier, // need to rename this modifier to prevent linter false positives
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
            horizontalArrangement = spacedBy(4.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.height(14.dp),
                imageVector = CompoundIcons.Threads,
                contentDescription = null,
                tint = ElementTheme.colors.iconSecondary,
            )
            Text(
                text = stringResource(CommonStrings.common_thread),
                style = ElementTheme.typography.fontBodyXsRegular,
                color = ElementTheme.colors.textPrimary,
            )
        }
    }

    @Composable
    fun WithTimestampLayout(
        timestampPosition: TimestampPosition,
        modifier: Modifier = Modifier,
        canShrinkContent: Boolean = false,
        content: @Composable (onContentLayoutChanged: (ContentAvoidingLayoutData) -> Unit) -> Unit,
    ) {
        when (timestampPosition) {
            TimestampPosition.Overlay ->
                Box(modifier, contentAlignment = Alignment.Center) {
                    content {}
                    TimelineEventTimestampView(
                        event = event,
                        onClick = onTimestampClicked,
                        onLongClick = ::onTimestampLongClick,
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 4.dp) // Outer padding
                            .background(ElementTheme.colors.bgSubtleSecondary, RoundedCornerShape(10.0.dp))
                            .align(Alignment.BottomEnd)
                            .padding(horizontal = 4.dp, vertical = 2.dp) // Inner padding
                    )
                }
            TimestampPosition.Aligned ->
                ContentAvoidingLayout(
                    modifier = modifier,
                    // The spacing is negative to make the content overlap the empty space at the start of the timestamp
                    spacing = -TimelineEventTimestampViewDefaults.spacing,
                    shrinkContent = canShrinkContent,
                ) {
                    content(this::onContentLayoutChanged)
                    TimelineEventTimestampView(
                        event = event,
                        onClick = onTimestampClicked,
                        onLongClick = ::onTimestampLongClick,
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            TimestampPosition.Below ->
                Column(modifier) {
                    content {}
                    TimelineEventTimestampView(
                        event = event,
                        onClick = onTimestampClicked,
                        onLongClick = ::onTimestampLongClick,
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
        val context = LocalContext.current
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
                canShrinkContent = canShrinkContent,
                modifier = timestampLayoutModifier,
            ) { onContentLayoutChanged ->
                TimelineItemEventContentView(
                    content = event.content,
                    onLinkClicked = { url ->
                        when (val permalink = PermalinkParser.parse(Uri.parse(url))) {
                            is PermalinkData.UserLink -> {
                                onMentionClicked(Mention.User(UserId(permalink.userId)))
                            }
                            is PermalinkData.RoomLink -> {
                                onMentionClicked(Mention.Room(permalink.getRoomId(), permalink.getRoomAlias()))
                            }
                            is PermalinkData.FallbackLink,
                            is PermalinkData.RoomEmailInviteLink -> {
                                context.openUrlInExternalApp(url)
                            }
                        }
                    },
                    eventSink = eventSink,
                    onContentLayoutChanged = onContentLayoutChanged,
                    modifier = contentModifier
                )
            }
        }
        val inReplyTo = @Composable { inReplyTo: InReplyToDetails ->
            val senderName = inReplyTo.senderDisplayName ?: inReplyTo.senderId.value
            val topPadding = if (showThreadDecoration) 0.dp else 8.dp
            ReplyToContent(
                senderName = senderName,
                metadata = inReplyTo.metadata(),
                modifier = Modifier
                    .padding(top = topPadding, start = 8.dp, end = 8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(enabled = true, onClick = inReplyToClick),
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
            Column(modifier = modifier, verticalArrangement = spacedBy(8.dp)) {
                threadDecoration()
                contentWithTimestamp()
            }
        }
    }

    val timestampPosition = when (event.content) {
        is TimelineItemImageContent,
        is TimelineItemStickerContent,
        is TimelineItemVideoContent,
        is TimelineItemLocationContent -> TimestampPosition.Overlay
        is TimelineItemPollContent -> TimestampPosition.Below
        else -> TimestampPosition.Default
    }
    CommonLayout(
        showThreadDecoration = event.isThreaded,
        timestampPosition = timestampPosition,
        inReplyToDetails = event.inReplyTo,
        canShrinkContent = event.content is TimelineItemVoiceContent,
        modifier = bubbleModifier
    )
}

@Composable
private fun ReplyToContent(
    senderName: String,
    metadata: InReplyToMetadata?,
    modifier: Modifier = Modifier,
) {
    val paddings = if (metadata is InReplyToMetadata.Thumbnail) {
        PaddingValues(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp)
    } else {
        PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    }
    Row(
        modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(paddings)
    ) {
        if (metadata is InReplyToMetadata.Thumbnail) {
            AttachmentThumbnail(
                info = metadata.attachmentThumbnailInfo,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Column(verticalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = senderName,
                style = ElementTheme.typography.fontBodySmMedium,
                textAlign = TextAlign.Start,
                color = ElementTheme.materialColors.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = metadata?.text.orEmpty(),
                style = ElementTheme.typography.fontBodyMdRegular,
                textAlign = TextAlign.Start,
                color = ElementTheme.materialColors.secondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowPreview() = ElementPreview {
    Column {
        sequenceOf(false, true).forEach {
            ATimelineItemEventRow(
                event = aTimelineItemEvent(
                    isMine = it,
                    content = aTimelineItemTextContent().copy(
                        body = "A long text",
                    ),
                    groupPosition = TimelineItemGroupPosition.First,
                ),
            )
            ATimelineItemEventRow(
                event = aTimelineItemEvent(
                    isMine = it,
                    content = aTimelineItemImageContent().copy(
                        aspectRatio = 2.5f
                    ),
                    groupPosition = TimelineItemGroupPosition.Last,
                ),
            )
        }
    }
}
