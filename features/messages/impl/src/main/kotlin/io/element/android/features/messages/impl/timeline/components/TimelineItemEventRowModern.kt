/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.timeline.TimelineEvent
import io.element.android.features.messages.impl.timeline.TimelineRoomInfo
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayout
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.TimelineItemThreadInfo
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemPollContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStickerContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import io.element.android.features.messages.impl.timeline.protection.TimelineProtectionState
import io.element.android.libraries.designsystem.colors.AvatarColorsProvider
import io.element.android.libraries.designsystem.components.EqualWidthColumn
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToDetails
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToView
import io.element.android.libraries.matrix.ui.messages.reply.eventId
import io.element.android.libraries.matrix.ui.messages.sender.SenderName
import io.element.android.libraries.matrix.ui.messages.sender.SenderNameMode
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.libraries.ui.utils.time.isTalkbackActive

@Composable
internal fun TimelineItemEventRowModernContent(
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
    // In Modern layout, show avatar+name for ALL senders (including own) at group boundaries,
    // matching Element Classic behavior. The Bubble-only `showSenderInformation` excludes isMine.
    // Grouping is by sender continuity (same as bubble layout and Element Classic).
    // No time-based threshold — consecutive messages from same sender are always grouped.
    val showSenderInfo = event.groupPosition.isNew()

    // Avatar column width: 32dp avatar + 8dp gap = 40dp
    val avatarColumnWidth = 40.dp
    // Content indent from screen edge: 16dp padding + 40dp avatar column = 56dp
    val contentIndent = 16.dp + avatarColumnWidth

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            // Avatar column
            Box(
                modifier = Modifier
                    .width(avatarColumnWidth)
                    .padding(top = 2.dp),
                contentAlignment = Alignment.TopStart,
            ) {
                if (showSenderInfo) {
                    Avatar(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag(TestTags.timelineItemSenderAvatar)
                            .clip(CircleShape)
                            .clickable(onClick = onUserDataClick),
                        avatarData = event.senderAvatar,
                        avatarType = AvatarType.User,
                    )
                }
            }

            // Content column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .combinedClickable(
                        interactionSource = interactionSource,
                        indication = remember { ripple() },
                        onClick = onContentClick,
                        onLongClick = onLongClick,
                    ),
            ) {
                // Sender name + timestamp on same row (matching Classic Modern layout)
                if (showSenderInfo) {
                    val avatarColors = AvatarColorsProvider.provide(event.senderAvatar.id)
                    Row(
                        modifier = Modifier.padding(bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SenderName(
                            modifier = Modifier
                                .testTag(TestTags.timelineItemSenderName)
                                .weight(1f)
                                .clearAndSetSemantics { hideFromAccessibility() },
                            senderId = event.senderId,
                            senderProfile = event.senderProfile,
                            senderNameMode = SenderNameMode.Timeline(avatarColors.foreground),
                        )
                        TimelineEventTimestampView(
                            event = event,
                            eventSink = eventSink,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }

                // Message content with timestamp (for non-header messages, timestamp stays in content)
                ModernMessageContent(
                    event = event,
                    timelineMode = timelineMode,
                    timelineProtectionState = timelineProtectionState,
                    onMessageLongClick = onLongClick,
                    inReplyToClick = inReplyToClick,
                    showTimestampInContent = !showSenderInfo || timelineRoomInfo.isDm,
                    eventSink = eventSink,
                    eventContentView = eventContentView,
                )
            }
        }

        // Pin icon
        val isEventPinned = timelineRoomInfo.pinnedEventIds.contains(event.eventId)
        if (isEventPinned) {
            Icon(
                imageVector = CompoundIcons.PinSolid(),
                contentDescription = stringResource(CommonStrings.common_pinned),
                tint = ElementTheme.colors.iconTertiary,
                modifier = Modifier
                    .padding(start = if (timelineRoomInfo.isDm) 16.dp else contentIndent, top = 2.dp)
                    .size(16.dp),
            )
        }

        // Reactions
        if (event.reactionsState.reactions.isNotEmpty()) {
            TimelineItemReactionsView(
                reactionsState = event.reactionsState,
                userCanSendReaction = timelineRoomInfo.userHasPermissionToSendReaction,
                isOutgoing = false,
                onReactionClick = onReactionClick,
                onReactionLongClick = onReactionLongClick,
                onMoreReactionsClick = { onMoreReactionsClick(event) },
                modifier = Modifier
                    .zIndex(1f)
                    .padding(
                        start = if (timelineRoomInfo.isDm) 16.dp else contentIndent,
                        end = 16.dp,
                        top = 4.dp,
                    )
            )
        }
    }
}

@Suppress("MultipleEmitters") // False positive
@Composable
private fun ModernMessageContent(
    event: TimelineItem.Event,
    timelineMode: Timeline.Mode,
    timelineProtectionState: TimelineProtectionState,
    onMessageLongClick: () -> Unit,
    inReplyToClick: () -> Unit,
    showTimestampInContent: Boolean,
    eventSink: (TimelineEvent.TimelineItemEvent) -> Unit,
    @SuppressLint("ModifierParameter")
    @Suppress("ModifierNaming")
    contentModifier: Modifier = Modifier,
    eventContentView: @Composable (Modifier, (ContentAvoidingLayoutData) -> Unit) -> Unit,
) {
    @Composable
    fun WithTimestampLayout(
        timestampPosition: TimestampPosition,
        eventSink: (TimelineEvent.TimelineItemEvent) -> Unit,
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
                            .background(ElementTheme.colors.bgSubtleSecondary, MaterialTheme.shapes.small)
                            .align(Alignment.BottomEnd)
                            // Inner padding
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            TimestampPosition.Aligned ->
                ContentAvoidingLayout(
                    modifier = modifier.fillMaxWidth(),
                    spacing = (-4).dp,
                    overlayOffset = DpOffset(0.dp, -1.dp),
                    shrinkContent = canShrinkContent,
                    content = { content(this::onContentLayoutChange) },
                    overlay = {
                        TimelineEventTimestampView(
                            event = event,
                            eventSink = eventSink,
                            modifier = Modifier
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                        )
                    }
                )
            TimestampPosition.Below ->
                Column(modifier.fillMaxWidth()) {
                    content {}
                    TimelineEventTimestampView(
                        event = event,
                        eventSink = eventSink,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(vertical = 4.dp)
                    )
                }
        }
    }

    @Composable
    fun CommonLayout(
        timestampPosition: TimestampPosition?,
        showThreadDecoration: Boolean,
        inReplyToDetails: InReplyToDetails?,
        modifier: Modifier = Modifier,
        canShrinkContent: Boolean = false,
    ) {
        val contentModifier = Modifier.padding(top = if (inReplyToDetails != null) 0.dp else 4.dp, bottom = 4.dp)

        val threadDecoration = @Composable {
            if (showThreadDecoration) {
                TimelineThreadDecoration(modifier = Modifier.padding(top = 4.dp))
            }
        }
        val contentWithTimestamp = @Composable {
            if (timestampPosition != null) {
                WithTimestampLayout(
                    timestampPosition = timestampPosition,
                    eventSink = eventSink,
                    canShrinkContent = canShrinkContent,
                    modifier = Modifier.semantics(mergeDescendants = false) {
                        isTraversalGroup = true
                        traversalIndex = -1f
                    },
                    content = { onContentLayoutChange ->
                        eventContentView(contentModifier, onContentLayoutChange)
                    }
                )
            } else {
                // Timestamp already shown in sender name header row
                eventContentView(contentModifier) {}
            }
        }

        val inReplyTo = @Composable { inReplyTo: InReplyToDetails ->
            val inReplyToModifier = Modifier
                .padding(top = if (showThreadDecoration) 0.dp else 4.dp)
                .clip(MaterialTheme.shapes.extraSmall)

            val talkbackCompatModifier = if (isTalkbackActive()) {
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
            EqualWidthColumn(spacing = 4.dp) {
                threadDecoration()
                inReplyTo(inReplyToDetails)
                contentWithTimestamp()
            }
        } else {
            Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                threadDecoration()
                contentWithTimestamp()
            }
        }
    }

    // For media types, always show timestamp in content. For text, only show if not in header.
    val timestampPosition = when (event.content) {
        is TimelineItemImageContent -> if (event.content.showCaption) TimestampPosition.Aligned else TimestampPosition.Overlay
        is TimelineItemVideoContent -> if (event.content.showCaption) TimestampPosition.Aligned else TimestampPosition.Overlay
        is TimelineItemStickerContent,
        is TimelineItemLocationContent -> TimestampPosition.Below
        is TimelineItemPollContent -> TimestampPosition.Below
        else -> TimestampPosition.Aligned
    }
    // When timestamp is shown in the sender name header, skip it in content for text-like messages
    val effectiveTimestampPosition = if (!showTimestampInContent && timestampPosition == TimestampPosition.Aligned) {
        null // No timestamp in content
    } else {
        timestampPosition
    }
    CommonLayout(
        showThreadDecoration = timelineMode !is Timeline.Mode.Thread && event.threadInfo is TimelineItemThreadInfo.ThreadResponse,
        timestampPosition = effectiveTimestampPosition,
        inReplyToDetails = event.inReplyTo,
        canShrinkContent = event.content is TimelineItemVoiceContent,
        modifier = contentModifier,
    )
}
