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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.element.android.features.messages.impl.timeline.components.event.TimelineItemEventContentView
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.bubble.BubbleState
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.libraries.designsystem.ElementTextStyles
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.theme.LocalColors
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

@Composable
fun TimelineItemEventRow(
    event: TimelineItem.Event,
    isHighlighted: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onUserDataClick: (UserId) -> Unit,
    inReplyToClick: (EventId) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    fun onUserDataClicked() {
        onUserDataClick(event.senderId)
    }

    fun inReplayToClicked() {
        val inReplyToEventId = (event.inReplyTo as? InReplyTo.Ready)?.eventId ?: return
        inReplyToClick(inReplyToEventId)
    }

    val (parentAlignment, contentAlignment) = if (event.isMine) {
        Pair(Alignment.CenterEnd, Alignment.End)
    } else {
        Pair(Alignment.CenterStart, Alignment.Start)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = parentAlignment
    ) {
        Row {
            if (!event.isMine) {
                Spacer(modifier = Modifier.width(4.dp))
            }
            Column(horizontalAlignment = contentAlignment) {
                if (event.showSenderInformation) {
                    MessageSenderInformation(
                        event.safeSenderName,
                        event.senderAvatar,
                        Modifier
                            .zIndex(1f)
                            .offset(y = 12.dp)
                            .clickable(onClick = ::onUserDataClicked)
                    )
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
                    modifier = Modifier
                        .zIndex(-1f)
                        .widthIn(max = 320.dp)
                ) {
                    MessageEventBubbleContent(
                        event = event,
                        interactionSource = interactionSource,
                        onMessageClick = onClick,
                        onMessageLongClick = onLongClick,
                        inReplyToClick = ::inReplayToClicked,
                    )
                }
                TimelineItemReactionsView(
                    reactionsState = event.reactionsState,
                    modifier = Modifier
                        .zIndex(1f)
                        .offset(x = if (event.isMine) 0.dp else 20.dp, y = -(4.dp))
                )
            }
            if (event.isMine) {
                Spacer(modifier = Modifier.width(16.dp))
            }
        }
    }
    if (event.groupPosition.isNew()) {
        Spacer(modifier = modifier.height(8.dp))
    } else {
        Spacer(modifier = modifier.height(2.dp))
    }
}

@Composable
private fun MessageSenderInformation(
    sender: String,
    senderAvatar: AvatarData?,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        if (senderAvatar != null) {
            Avatar(senderAvatar)
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = sender,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .alignBy(LastBaseline)
        )
    }
}

@Composable
private fun MessageEventBubbleContent(
    event: TimelineItem.Event,
    interactionSource: MutableInteractionSource,
    onMessageClick: () -> Unit,
    onMessageLongClick: () -> Unit,
    inReplyToClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isMediaItem = event.content is TimelineItemImageContent || event.content is TimelineItemVideoContent
    val replyToDetails = event.inReplyTo as? InReplyTo.Ready

    @Composable
    fun ContentView(
        modifier: Modifier = Modifier
    ) {
        TimelineItemEventContentView(
            content = event.content,
            interactionSource = interactionSource,
            onClick = onMessageClick,
            onLongClick = onMessageLongClick,
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
                    onClick = onMessageClick,
                    modifier = timestampModifier
                        .padding(horizontal = 4.dp, vertical = 4.dp) // Outer padding
                        .background(LocalColors.current.gray300, RoundedCornerShape(10.0.dp))
                        .align(Alignment.BottomEnd)
                        .padding(horizontal = 4.dp, vertical = 2.dp) // Inner padding
                )
            }
        } else {
            Column(modifier) {
                ContentView(modifier = contentModifier.padding(start = 12.dp, end = 12.dp, top = 8.dp))
                TimelineEventTimestampView(
                    event = event,
                    onClick = onMessageClick,
                    modifier = timestampModifier
                        .align(Alignment.End)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
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
        Column(modifier.width(IntrinsicSize.Max), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (inReplyToDetails != null) {
                val senderName = event.senderDisplayName ?: event.senderId.value
                val attachmentThumbnailInfo = attachmentThumbnailInfoForInReplyTo(inReplyToDetails)
                ReplyToContent(
                    senderName = senderName,
                    text = inReplyToDetails.content.body,
                    attachmentThumbnailInfo = attachmentThumbnailInfo,
                    modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp)
                        .fillMaxWidth()
                        .clickable(enabled = true, onClick = inReplyToClick),
                )
            }
            val contentModifier = if (isMediaItem) {
                Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            } else {
                Modifier
            }
            ContentAndTimestampView(overlayTimestamp = isMediaItem, contentModifier = contentModifier)
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
            .clip(RoundedCornerShape(6.dp))
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
                senderName,
                style = ElementTextStyles.Regular.caption2.copy(fontWeight = FontWeight.Medium),
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = text.orEmpty(),
                style = ElementTextStyles.Regular.caption1,
                textAlign = TextAlign.Start,
                color = LocalColors.current.placeholder,
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
