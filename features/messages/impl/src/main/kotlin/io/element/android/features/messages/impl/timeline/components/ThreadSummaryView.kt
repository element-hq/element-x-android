/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.modifiers.niceClickable
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.preview.USER_NAME_ALICE
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.EmbeddedEventInfo
import io.element.android.libraries.matrix.api.timeline.item.ThreadSummary
import io.element.android.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileDetails
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.getAvatarUrl
import io.element.android.libraries.matrix.api.timeline.item.event.getDisambiguatedDisplayName
import io.element.android.libraries.matrix.api.timeline.item.event.getDisplayName
import io.element.android.libraries.ui.strings.CommonPlurals

@Composable
internal fun ThreadSummaryView(
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
                        displayName = USER_NAME_ALICE,
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
