/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.messages.api.timeline.TimelineSampleRenderer
import io.element.android.features.messages.impl.R
import io.element.android.features.messages.impl.timeline.components.TimelineItemRow
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition
import io.element.android.features.messages.impl.timeline.model.TimelineItemReactions
import io.element.android.features.messages.impl.timeline.model.TimelineItemSelectionData
import io.element.android.features.messages.impl.timeline.model.anAggregatedReaction
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.features.messages.impl.timeline.protection.aTimelineProtectionState
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileDetails
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToDetails
import io.element.android.libraries.preferences.api.store.MessageTextSize
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@ContributesBinding(AppScope::class)
class DefaultTimelineSampleRenderer : TimelineSampleRenderer {
    @Composable
    override fun Render(textSizeSp: Int, modifier: Modifier) {
        val timelineItems = rememberSampleTimelineItems()
        val roomInfo = remember { aTimelineRoomInfo(userHasPermissionToSendReaction = false) }
        val protectionState = remember { aTimelineProtectionState() }
        val rendererConfig = remember(textSizeSp) {
            ComposeLocalTimelineEventRendererConfig(
                isComposeRendererEnabled = true,
                currentUserId = null,
                fontScale = MessageTextSize.scaleOf(textSizeSp),
            )
        }
        val density = LocalDensity.current
        val scaledDensity = remember(density, textSizeSp) {
            Density(density.density, density.fontScale * MessageTextSize.scaleOf(textSizeSp))
        }
        CompositionLocalProvider(
            LocalTimelineEventRendererConfig provides rendererConfig,
            LocalDensity provides scaledDensity,
        ) {
            // Deliberately not using TimelineView
            LazyColumn(
                modifier = modifier,
                reverseLayout = true,
            ) {
                items(timelineItems.asReversed()) { timelineItem ->
                    TimelineItemRow(
                        timelineItem = timelineItem,
                        timelineMode = Timeline.Mode.Live,
                        timelineRoomInfo = roomInfo,
                        timelineProtectionState = protectionState,
                        isLastOutgoingMessage = false,
                        focusedEventId = null,
                        displayThreadSummaries = false,
                        selectionData = TimelineItemSelectionData(),
                        onUserDataClick = {},
                        onLinkClick = {},
                        onLinkLongClick = {},
                        onContentClick = {},
                        onGalleryItemClick = { _, _ -> },
                        onLongClick = {},
                        inReplyToClick = {},
                        onReactionClick = { _, _ -> },
                        onReactionLongClick = { _, _ -> },
                        onMoreReactionsClick = {},
                        onReadReceiptClick = {},
                        onSwipeToReply = {},
                        onJoinCallClick = {},
                        eventSink = {},
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberSampleTimelineItems(): ImmutableList<TimelineItem> {
    val sender = stringResource(R.string.screen_message_text_size_sample_sender)
    val recipient = stringResource(R.string.screen_message_text_size_sample_recipient)
    val question = stringResource(R.string.screen_message_text_size_sample_question)
    val answer = stringResource(R.string.screen_message_text_size_sample_answer)
    return remember(sender, recipient, question, answer) {
        persistentListOf(
            aTimelineItemDaySeparator(),
            aTimelineItemEvent(
                isMine = true,
                senderDisplayName = recipient,
                content = aTimelineItemTextContent(body = question),
                groupPosition = TimelineItemGroupPosition.None,
                // The fixture adds one by default; the design only reacts to the reply below.
                timelineItemReactions = TimelineItemReactions(persistentListOf()),
            ),
            aTimelineItemEvent(
                isMine = false,
                senderDisplayName = sender,
                content = aTimelineItemTextContent(body = answer),
                groupPosition = TimelineItemGroupPosition.None,
                inReplyTo = aSampleReply(sender = recipient, body = question),
                timelineItemReactions = TimelineItemReactions(persistentListOf(anAggregatedReaction(key = "😁"))),
            ),
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
