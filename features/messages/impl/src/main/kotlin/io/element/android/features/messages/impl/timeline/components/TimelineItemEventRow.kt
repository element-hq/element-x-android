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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.aTimelineItemReactions
import io.element.android.features.messages.impl.timeline.components.event.TimelineItemEventContentView
import io.element.android.features.messages.impl.timeline.components.event.toExtraPadding
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.bubble.BubbleState
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.libraries.designsystem.components.EqualWidthColumn
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.event.FileMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.ImageMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.InReplyTo
import io.element.android.libraries.matrix.api.timeline.item.event.VideoMessageType
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnail
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnailInfo
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnailType
import io.element.android.libraries.theme.ElementTheme
import org.jsoup.Jsoup

@Composable
fun TimelineItemEventRow(
    event: TimelineItem.Event,
    isHighlighted: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onUserDataClick: (UserId) -> Unit,
    inReplyToClick: (EventId) -> Unit,
    onTimestampClicked: (TimelineItem.Event) -> Unit,
    onReactionClick: (emoji: String, eventId: TimelineItem.Event) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    fun onUserDataClicked() {
        onUserDataClick(event.senderId)
    }

    fun onReactionClicked(emoji: String) =
        onReactionClick(emoji, event)

    fun inReplyToClicked() {
        val inReplyToEventId = (event.inReplyTo as? InReplyTo.Ready)?.eventId ?: return
        inReplyToClick(inReplyToEventId)
    }

    // To avoid using negative offset, we display in this Box a column with:
    // - Spacer to give room to the Sender information if they must be displayed;
    // - The message bubble;
    // - Spacer for the reactions if there are some.
    // Then the Sender information and the reactions are displayed on top of it.
    // This fixes some clickable issue and some unexpected margin on top and bottom of each message row
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = if (event.isMine) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column {
            if (event.showSenderInformation) {
                Spacer(modifier = Modifier.height(event.senderAvatar.size.dp - 8.dp))
            }
            val bubbleState = BubbleState(
                groupPosition = event.groupPosition,
                isMine = event.isMine,
                isHighlighted = isHighlighted,
            )
            MessageEventBubble(
                state = bubbleState,
                interactionSource = interactionSource,
                onClick = onClick,
                onLongClick = onLongClick,
            ) {
                MessageEventBubbleContent(
                    event = event,
                    interactionSource = interactionSource,
                    onMessageClick = onClick,
                    onMessageLongClick = onLongClick,
                    inReplyToClick = ::inReplyToClicked,
                    onTimestampClicked = {
                        onTimestampClicked(event)
                    }
                )
            }
            if (event.reactionsState.reactions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
        // Align to the top of the box
        if (event.showSenderInformation) {
            MessageSenderInformation(
                event.safeSenderName,
                event.senderAvatar,
                Modifier
                    .padding(horizontal = 16.dp)
                    .align(Alignment.TopStart)
                    .clickable(onClick = ::onUserDataClicked)
            )
        }
        // Align to the bottom of the box
        if (event.reactionsState.reactions.isNotEmpty()) {
            TimelineItemReactionsView(
                reactionsState = event.reactionsState,
                onReactionClicked = ::onReactionClicked,
                modifier = Modifier
                    .align(if (event.isMine) Alignment.BottomEnd else Alignment.BottomStart)
                    .padding(start = if (event.isMine) 16.dp else 36.dp, end = 16.dp)
            )
        }
    }
    // This is assuming that we are in a ColumnScope, but this is OK, for both Preview and real usage.
    if (event.groupPosition.isNew()) {
        Spacer(modifier = modifier.height(16.dp))
    } else {
        Spacer(modifier = modifier.height(2.dp))
    }
}

@Composable
private fun MessageSenderInformation(
    sender: String,
    senderAvatar: AvatarData,
    modifier: Modifier = Modifier
) {
    val avatarStrokeSize = 3.dp
    val avatarStrokeColor = MaterialTheme.colorScheme.background
    val avatarSize = senderAvatar.size.dp
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
                text = sender,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun MessageEventBubbleContent(
    event: TimelineItem.Event,
    interactionSource: MutableInteractionSource,
    onMessageClick: () -> Unit,
    onMessageLongClick: () -> Unit,
    inReplyToClick: () -> Unit,
    onTimestampClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isMediaItem = event.content is TimelineItemImageContent || event.content is TimelineItemVideoContent
    val replyToDetails = event.inReplyTo as? InReplyTo.Ready

    // Long clicks are not not automatically propagated from a `clickable`
    // to its `combinedClickable` parent so we do it manually
    fun onTimestampLongClick() = onMessageLongClick()

    @Composable
    fun ContentView(
        modifier: Modifier = Modifier
    ) {
        TimelineItemEventContentView(
            content = event.content,
            interactionSource = interactionSource,
            onClick = onMessageClick,
            onLongClick = onMessageLongClick,
            extraPadding = event.toExtraPadding(),
            modifier = modifier,
        )
    }

    @Composable
    fun ContentAndTimestampView(
        overlayTimestamp: Boolean,
        modifier: Modifier = Modifier,
        contentModifier: Modifier = Modifier,
        timestampModifier: Modifier = Modifier,
    ) {
        if (overlayTimestamp) {
            Box(modifier) {
                ContentView(modifier = contentModifier)
                TimelineEventTimestampView(
                    event = event,
                    onClick = onTimestampClicked,
                    onLongClick = ::onTimestampLongClick,
                    modifier = timestampModifier
                        .padding(horizontal = 4.dp, vertical = 4.dp) // Outer padding
                        .background(ElementTheme.legacyColors.gray300, RoundedCornerShape(10.0.dp))
                        .align(Alignment.BottomEnd)
                        .padding(horizontal = 4.dp, vertical = 2.dp) // Inner padding
                )
            }
        } else {
            Box(modifier) {
                ContentView(modifier = contentModifier.padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp))
                TimelineEventTimestampView(
                    event = event,
                    onClick = onTimestampClicked,
                    onLongClick = ::onTimestampLongClick,
                    modifier = timestampModifier
                        .align(Alignment.BottomEnd)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }

    /** Used only for media items, with no reply to metadata. It displays the contents with no paddings. */
    @Composable
    fun SimpleMediaItemLayout(modifier: Modifier = Modifier) {
        ContentAndTimestampView(overlayTimestamp = true, modifier = modifier)
    }

    /** Used for every other type of message, groups the different components in a Column with some space between them. */
    @Composable
    fun CommonLayout(
        inReplyToDetails: InReplyTo.Ready?,
        modifier: Modifier = Modifier
    ) {
        EqualWidthColumn(modifier = modifier, spacing = 8.dp) {
            if (inReplyToDetails != null) {
                val senderName = inReplyToDetails.senderDisplayName ?: inReplyToDetails.senderId.value
                val attachmentThumbnailInfo = attachmentThumbnailInfoForInReplyTo(inReplyToDetails)
                ReplyToContent(
                    senderName = senderName,
                    text = inReplyToDetails.content.body,
                    attachmentThumbnailInfo = attachmentThumbnailInfo,
                    modifier = Modifier
                        .padding(top = 8.dp, start = 8.dp, end = 8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(enabled = true, onClick = inReplyToClick),
                )
            }
            val modifierWithPadding = if (isMediaItem) {
                Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            } else {
                Modifier
            }

            val contentModifier = if (isMediaItem) {
                Modifier.clip(RoundedCornerShape(12.dp))
            } else {
                Modifier
            }

            ContentAndTimestampView(
                overlayTimestamp = isMediaItem,
                contentModifier = contentModifier,
                modifier = modifierWithPadding,
            )
        }
    }

    if (isMediaItem && replyToDetails == null) {
        SimpleMediaItemLayout()
    } else {
        CommonLayout(inReplyToDetails = replyToDetails, modifier = modifier)
    }
}

@Composable
private fun ReplyToContent(
    senderName: String,
    text: String?,
    attachmentThumbnailInfo: AttachmentThumbnailInfo?,
    modifier: Modifier = Modifier,
) {
    val paddings = if (attachmentThumbnailInfo != null) {
        PaddingValues(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp)
    } else {
        PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 4.dp)
    }
    Row(
        modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(paddings)
    ) {
        if (attachmentThumbnailInfo != null) {
            AttachmentThumbnail(
                info = attachmentThumbnailInfo,
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
                text = text.orEmpty(),
                style = ElementTheme.typography.fontBodyMdRegular,
                textAlign = TextAlign.Start,
                color = ElementTheme.materialColors.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun attachmentThumbnailInfoForInReplyTo(inReplyTo: InReplyTo.Ready) =
    when (val type = inReplyTo.content.type) {
        is ImageMessageType -> AttachmentThumbnailInfo(
            mediaSource = type.info?.thumbnailSource,
            textContent = inReplyTo.content.body,
            type = AttachmentThumbnailType.Image,
            blurHash = type.info?.blurhash,
        )
        is VideoMessageType -> AttachmentThumbnailInfo(
            mediaSource = type.info?.thumbnailSource,
            textContent = inReplyTo.content.body,
            type = AttachmentThumbnailType.Video,
            blurHash = type.info?.blurhash,
        )
        is FileMessageType -> AttachmentThumbnailInfo(
            mediaSource = type.info?.thumbnailSource,
            textContent = inReplyTo.content.body,
            type = AttachmentThumbnailType.File,
            blurHash = null,
        )
        else -> null
    }

@Preview
@Composable
internal fun TimelineItemEventRowLightPreview() =
    ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
internal fun TimelineItemEventRowDarkPreview() =
    ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    Column {
        sequenceOf(false, true).forEach {
            TimelineItemEventRow(
                event = aTimelineItemEvent(
                    isMine = it,
                    content = aTimelineItemTextContent().copy(
                        body = "A long text which will be displayed on several lines and" +
                            " hopefully can be manually adjusted to test different behaviors."
                    )
                ),
                isHighlighted = false,
                onClick = {},
                onLongClick = {},
                onUserDataClick = {},
                inReplyToClick = {},
                onReactionClick = { _, _ -> },
                onTimestampClicked = {},
            )
            TimelineItemEventRow(
                event = aTimelineItemEvent(
                    isMine = it,
                    content = aTimelineItemImageContent().copy(
                        aspectRatio = 5f
                    )
                ),
                isHighlighted = false,
                onClick = {},
                onLongClick = {},
                onUserDataClick = {},
                inReplyToClick = {},
                onReactionClick = { _, _ -> },
                onTimestampClicked = {},
            )
        }
    }
}

@Preview
@Composable
internal fun TimelineItemEventRowTimestampLightPreview(@PreviewParameter(TimelineItemEventForTimestampViewProvider::class) event: TimelineItem.Event) =
    ElementPreviewLight { ContentTimestampToPreview(event) }

@Preview
@Composable
internal fun TimelineItemEventRowTimestampDarkPreview(@PreviewParameter(TimelineItemEventForTimestampViewProvider::class) event: TimelineItem.Event) =
    ElementPreviewDark { ContentTimestampToPreview(event) }

@Composable
private fun ContentTimestampToPreview(event: TimelineItem.Event) {
    Column {
        val oldContent = event.content as TimelineItemTextContent
        listOf(
            "Text",
            "Text longer, displayed on 1 line",
            "Text which should be rendered on several lines",
        ).forEach { str ->
            listOf(false, true).forEach { useDocument ->
                TimelineItemEventRow(
                    event = event.copy(
                        content = oldContent.copy(
                            body = str,
                            htmlDocument = if (useDocument) Jsoup.parse(str) else null,
                        ),
                        reactionsState = aTimelineItemReactions(count = 0),
                        senderDisplayName = if (useDocument) "Document case" else "Text case",
                    ),
                    isHighlighted = false,
                    onClick = {},
                    onLongClick = {},
                    onUserDataClick = {},
                    inReplyToClick = {},
                    onReactionClick = { _, _ -> },
                    onTimestampClicked = {},
                )
            }
        }
    }
}
