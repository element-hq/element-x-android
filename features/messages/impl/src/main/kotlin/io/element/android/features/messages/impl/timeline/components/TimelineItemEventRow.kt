/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstrainScope
import androidx.constraintlayout.compose.ConstraintLayout
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.timeline.TimelineEvent
import io.element.android.features.messages.impl.timeline.TimelineRoomInfo
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.components.event.TimelineItemEventContentView
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.components.receipt.ReadReceiptViewState
import io.element.android.features.messages.impl.timeline.components.receipt.TimelineItemReadReceiptView
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition
import io.element.android.features.messages.impl.timeline.model.TimelineItemReactions
import io.element.android.features.messages.impl.timeline.model.TimelineItemThreadInfo
import io.element.android.features.messages.impl.timeline.model.bubble.BubbleState
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemAttachmentsContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemGalleryContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.features.messages.impl.timeline.protection.TimelineProtectionState
import io.element.android.features.messages.impl.timeline.protection.mustBeProtected
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewWithExtraLargeHeight
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.preview.USER_NAME_ALICE
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
import io.element.android.libraries.matrix.api.timeline.item.event.MessageShield
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileDetails
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.getAvatarUrl
import io.element.android.libraries.matrix.api.timeline.item.event.getDisplayName
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.media.contentvalidation.collectOverallState
import io.element.android.libraries.matrix.ui.media.contentvalidation.rememberEventContentValidationState
import io.element.android.libraries.matrix.ui.messages.reply.content
import io.element.android.libraries.matrix.ui.messages.reply.eventId
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.wysiwyg.link.Link
import kotlinx.collections.immutable.persistentListOf

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
    isLastOutgoingMessage: Boolean,
    displayThreadSummaries: Boolean,
    onEventClick: () -> Unit,
    onGalleryItemClick: ((Int) -> Unit),
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
    eventSink: (TimelineEvent.TimelineItemEvent) -> Unit,
    modifier: Modifier = Modifier,
    eventContentView: @Composable (Modifier, (ContentAvoidingLayoutData) -> Unit) -> Unit = { contentModifier, onContentLayoutChange ->
        // Only pass down a custom clickable lambda if the content can be clicked separately
        val onContentClick = onEventClick.takeUnless { event.isWholeContentClickable }

        TimelineItemEventContentView(
            eventId = event.eventId,
            content = event.content,
            timelineProtectionState = timelineProtectionState,
            onContentClick = onContentClick,
            onGalleryItemClick = onGalleryItemClick,
            onLongClick = onLongClick,
            onLinkClick = onLinkClick,
            onLinkLongClick = onLinkLongClick,
            eventSink = eventSink,
            modifier = contentModifier,
            onContentLayoutChange = onContentLayoutChange,
        )
    },
) {
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
        SwipeToReplyContainer(
            enabled = canReply,
            onSwipeToReply = onSwipeToReply,
        ) { contentModifier ->
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
                modifier = contentModifier,
                eventSink = eventSink,
                eventContentView = eventContentView,
            )
        }

        if (displayThreadSummaries && timelineMode !is Timeline.Mode.Thread && event.threadInfo is TimelineItemThreadInfo.ThreadRoot) {
            ThreadSummaryView(
                modifier = if (event.isMine) {
                    Modifier
                        .align(Alignment.End)
                        .padding(end = 16.dp)
                } else {
                    if (timelineRoomInfo.isDm) Modifier else Modifier.padding(start = 16.dp)
                }.padding(top = 2.dp),
                threadSummary = event.threadInfo.summary,
                latestEventText = event.threadInfo.latestEventText,
                isOutgoing = event.isMine,
                onClick = {
                    event.eventId?.let {
                        eventSink(TimelineEvent.OpenThread(it.toThreadId(), null))
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
            onReadReceiptsClick = { onReadReceiptClick(event) },
            modifier = Modifier.padding(top = 4.dp)
        )
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
    eventSink: (TimelineEvent.TimelineItemEvent) -> Unit,
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

        val currentContentValidationState by rememberEventContentValidationState(eventId = event.eventId, needsValidation = event.content.isMedia)
            .collectOverallState()
        val hasContentValidationError = currentContentValidationState.hasError()
        val needsInvalidContentCustomisations =
            // Gallery events should not apply the custom bubble color, instead each item will apply some custom color if needed
            event.content !is TimelineItemGalleryContent &&
                event.content !is TimelineItemAttachmentsContent &&
                hasContentValidationError &&
                event.content.isMedia

        // If the event has a dangerous media content we need to set custom message bubble background and border colors
        val themeColors = ElementTheme.colors
        val (dangerousContentBubbleColor, borderColor) = remember(themeColors.isLight, needsInvalidContentCustomisations, event.content.type) {
            val background = themeColors.bgCriticalSubtle.takeIf { needsInvalidContentCustomisations }
            val border = themeColors.borderCriticalSubtle.takeIf { needsInvalidContentCustomisations }
            background to border
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
            customBackgroundColor = dangerousContentBubbleColor,
            borderColor = borderColor,
        ) {
            MessageEventBubbleContent(
                event = event,
                timelineMode = timelineMode,
                timelineProtectionState = timelineProtectionState,
                hasContentValidationError = hasContentValidationError,
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
                        summary = ThreadSummary(
                            latestEvent = AsyncData.Success(
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
                                        displayName = USER_NAME_ALICE,
                                        avatarUrl = null,
                                        displayNameAmbiguous = false,
                                    ),
                                    timestamp = 0L,
                                )
                            ),
                            numberOfReplies = 20L,
                        )
                    )
                ),
                displayThreadSummaries = true,
            )
        }
    }
}

@PreviewWithExtraLargeHeight
@Composable
internal fun TimelineItemEventRowRtlContentPreview() = ElementPreview {
    Column {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .padding(8.dp),
            text = "LTR layout direction",
            textAlign = TextAlign.Center,
            style = ElementTheme.typography.fontHeadingMdBold,
        )
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            sequenceOf(false, true).forEach { isMine ->
                ATimelineItemEventRow(
                    event = aTimelineItemEvent(
                        senderDisplayName = "Sender with a super long name that should ellipsize",
                        isMine = isMine,
                        content = aTimelineItemTextContent(
                            body = "ظَة وَدَاع يَسْتَغْرِب فِيهَ"
                        ),
                        timelineItemReactions = TimelineItemReactions(persistentListOf()),
                        groupPosition = TimelineItemGroupPosition.First,
                    )
                )
                ATimelineItemEventRow(
                    event = aTimelineItemEvent(
                        senderDisplayName = "Sender with a super long name that should ellipsize",
                        isMine = isMine,
                        content = aTimelineItemTextContent(
                            body = "ظَة وَدَاع يَسْتَغْرِب فِيهَ",
                            isEdited = true,
                        ),
                        timelineItemReactions = TimelineItemReactions(persistentListOf()),
                        groupPosition = TimelineItemGroupPosition.Middle,
                        messageShield = MessageShield.UnknownDevice(isCritical = true),
                    )
                )
                ATimelineItemEventRow(
                    event = aTimelineItemEvent(
                        senderDisplayName = "Sender with a super long name that should ellipsize",
                        isMine = isMine,
                        content = aTimelineItemTextContent(
                            body = "ظَة وَدَاع \nيَسْتَغْرِب فِيهَ"
                        ),
                        timelineItemReactions = TimelineItemReactions(persistentListOf()),
                        groupPosition = TimelineItemGroupPosition.Middle,
                    )
                )
                ATimelineItemEventRow(
                    event = aTimelineItemEvent(
                        senderDisplayName = "Sender with a super long name that should ellipsize",
                        isMine = isMine,
                        content = aTimelineItemTextContent(
                            body = "ظَة وَدَاع يَسْتَغْرِب فِيهَا اَلشَّاعِر أَنْ لَا يَبْكِي مِنْ أَلَم اَلْفِرَاق،" +
                                " وَيَصِف حَالَة اَلْمُودِعِينَ"
                        ),
                        timelineItemReactions = TimelineItemReactions(persistentListOf()),
                        groupPosition = TimelineItemGroupPosition.Last,
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .padding(8.dp),
            text = "RTL layout direction",
            textAlign = TextAlign.Center,
            style = ElementTheme.typography.fontHeadingMdBold,
        )

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            sequenceOf(false, true).forEach { isMine ->
                ATimelineItemEventRow(
                    event = aTimelineItemEvent(
                        senderDisplayName = "Sender with a super long name that should ellipsize",
                        isMine = isMine,
                        content = aTimelineItemTextContent(
                            body = "ظَة وَدَاع يَسْتَغْرِب فِيهَا اَلشَّاعِر أَنْ لَا يَبْكِي مِنْ أَلَم اَلْفِرَاق،" +
                                " وَيَصِف حَالَة اَلْمُودِعِينَ",
                        ),
                        timelineItemReactions = TimelineItemReactions(persistentListOf()),
                        groupPosition = TimelineItemGroupPosition.First,
                    )
                )
                ATimelineItemEventRow(
                    event = aTimelineItemEvent(
                        senderDisplayName = "Sender with a super long name that should ellipsize",
                        isMine = isMine,
                        content = aTimelineItemTextContent(
                            body = "Testing\nLTR Line\nBreaks.",
                        ),
                        timelineItemReactions = TimelineItemReactions(persistentListOf()),
                        groupPosition = TimelineItemGroupPosition.Middle,
                    )
                )
                ATimelineItemEventRow(
                    event = aTimelineItemEvent(
                        senderDisplayName = "Sender with a super long name that should ellipsize",
                        isMine = isMine,
                        content = aTimelineItemTextContent(
                            body = "Testing a very long LTR text in an RTL layout."
                        ),
                        timelineItemReactions = TimelineItemReactions(persistentListOf()),
                        groupPosition = TimelineItemGroupPosition.Middle,
                    )
                )
                ATimelineItemEventRow(
                    event = aTimelineItemEvent(
                        senderDisplayName = "Sender with a super long name that should ellipsize",
                        isMine = isMine,
                        content = aTimelineItemTextContent(
                            body = "Testing LTR in RTL layout.",
                        ),
                        timelineItemReactions = TimelineItemReactions(persistentListOf()),
                        groupPosition = TimelineItemGroupPosition.Last,
                        messageShield = MessageShield.UnknownDevice(isCritical = true),
                    )
                )
                ATimelineItemEventRow(
                    event = aTimelineItemEvent(
                        senderDisplayName = "Sender with a super long name that should ellipsize",
                        isMine = isMine,
                        content = aTimelineItemTextContent(
                            body = "Testing LTR in RTL layout.",
                            isEdited = true,
                        ),
                        timelineItemReactions = TimelineItemReactions(persistentListOf()),
                        groupPosition = TimelineItemGroupPosition.Last,
                        messageShield = MessageShield.UnknownDevice(isCritical = true),
                    )
                )
            }
        }
    }
}
