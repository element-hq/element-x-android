/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.features.messages.api.timeline.TimelineSampleRenderer
import io.element.android.features.messages.impl.R
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.features.messages.impl.timeline.protection.aTimelineProtectionState
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileDetails
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToDetails
import io.element.android.libraries.preferences.api.store.MessageTextSize
import kotlinx.collections.immutable.persistentListOf

@ContributesBinding(AppScope::class)
@Inject
class DefaultTimelineSampleRenderer : TimelineSampleRenderer {
    @Composable
    override fun Render(textSizeSp: Int, modifier: Modifier) {
        val sender = stringResource(R.string.screen_message_text_size_sample_sender)
        val recipient = stringResource(R.string.screen_message_text_size_sample_recipient)
        val question = stringResource(R.string.screen_message_text_size_sample_question)
        val answer = stringResource(R.string.screen_message_text_size_sample_answer)
        val timelineItems = remember(sender, recipient, question, answer) {
            persistentListOf(
                aTimelineItemEvent(
                    isMine = false,
                    senderDisplayName = sender,
                    content = aTimelineItemTextContent(body = answer),
                    groupPosition = TimelineItemGroupPosition.None,
                    inReplyTo = aSampleReply(sender = recipient, body = question),
                    timelineItemReactions = aTimelineItemReactions(count = 1),
                ),
                aTimelineItemEvent(
                    isMine = true,
                    senderDisplayName = recipient,
                    content = aTimelineItemTextContent(body = question),
                    groupPosition = TimelineItemGroupPosition.None,
                ),
                aTimelineItemDaySeparator(),
            )
        }
        val timelineState = remember(timelineItems, textSizeSp) {
            aTimelineState(
                timelineItems = timelineItems,
                timelineFontScale = MessageTextSize.scaleOf(textSizeSp),
            )
        }
        val protectionState = remember { aTimelineProtectionState() }
        TimelineView(
            state = timelineState,
            timelineProtectionState = protectionState,
            onUserDataClick = {},
            onLinkClick = {},
            onContentClick = {},
            onGalleryItemClick = { _, _ -> },
            onMessageLongClick = {},
            onSwipeToReply = {},
            onReactionClick = { _, _ -> },
            onReactionLongClick = { _, _ -> },
            onMoreReactionsClick = {},
            onReadReceiptClick = {},
            onJoinCallClick = {},
            modifier = modifier,
        )
    }
}

private fun aSampleReply(sender: String, body: String) = InReplyToDetails.Ready(
    eventId = EventId($$"$sampleReplyEventId"),
    senderId = UserId("@sample:element.io"),
    eventContent = MessageContent(
        body = body,
        inReplyTo = null,
        isEdited = false,
        threadInfo = null,
        type = TextMessageType(body = body, formatted = null),
    ),
    textContent = body,
    senderProfile = ProfileDetails.Ready(
        displayName = sender,
        displayNameAmbiguous = false,
        avatarUrl = null,
        displayedStatus = null,
    ),
)
