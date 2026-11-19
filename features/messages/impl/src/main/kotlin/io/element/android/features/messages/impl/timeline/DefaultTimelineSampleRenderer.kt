/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import androidx.compose.foundation.layout.fillMaxSize
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
import io.element.android.features.messages.impl.timeline.model.AggregatedReaction
import io.element.android.features.messages.impl.timeline.model.AggregatedReactionSender
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.TimelineItemReactions
import io.element.android.features.messages.impl.timeline.model.TimelineItemReadReceipts
import io.element.android.features.messages.impl.timeline.model.TimelineItemSelectionData
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextContent
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemDaySeparatorModel
import io.element.android.features.messages.impl.timeline.protection.ProtectionState
import io.element.android.features.messages.impl.timeline.protection.TimelineProtectionState
import io.element.android.features.messages.impl.typing.TypingNotificationState
import io.element.android.features.roomcall.api.RoomCallState
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UniqueId
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
        TimelineSample(textSizeSp = textSizeSp, modifier = modifier)
    }
}

@Composable
private fun TimelineSample(textSizeSp: Int, modifier: Modifier = Modifier) {
    val timelineItems = rememberSampleTimelineItems()
    val roomInfo = remember { aSampleRoomInfo() }
    val protectionState = remember { TimelineProtectionState(ProtectionState.RenderAll) {} }
    val rendererConfig = remember(textSizeSp) {
        ComposeLocalTimelineEventRendererConfig(
            isComposeRendererEnabled = false,
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
        // Deliberately not TimelineView
        LazyColumn(
            modifier = modifier,
            // Lays out from the bottom up, so the newest message stays against the slider whether or not the rest fits above it.
            reverseLayout = true,
        ) {
            items(timelineItems) { timelineItem ->
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

@Composable
private fun rememberSampleTimelineItems(): ImmutableList<TimelineItem> {
    val sender = stringResource(R.string.screen_message_text_size_sample_sender)
    val recipient = stringResource(R.string.screen_message_text_size_sample_recipient)
    val question = stringResource(R.string.screen_message_text_size_sample_question)
    val answer = stringResource(R.string.screen_message_text_size_sample_answer)
    return remember(sender, recipient, question, answer) {
        persistentListOf(
            aSampleEvent(
                senderName = sender,
                body = answer,
                inReplyTo = aSampleReply(sender = recipient, body = question),
                reactions = persistentListOf(aSampleReaction()),
            ),
            aSampleEvent(senderName = recipient, body = question, isMine = true),
            TimelineItem.Virtual(
                id = UniqueId("sampleDaySeparator"),
                model = TimelineItemDaySeparatorModel(formattedDate = "Today"),
            ),
        )
    }
}

private fun aSampleRoomInfo() = TimelineRoomInfo(
    currentUserId = SAMPLE_CURRENT_USER_ID,
    isDm = false,
    renderReadReceipts = false,
    name = null,
    userHasPermissionToSendMessage = false,
    userHasPermissionToSendReaction = false,
    roomCallState = RoomCallState.Unavailable,
    pinnedEventIds = persistentListOf(),
    typingNotificationState = TypingNotificationState(
        renderTypingNotifications = false,
        typingMembers = persistentListOf(),
        reserveSpace = false,
    ),
    predecessorRoom = null,
)

private fun aSampleEvent(
    senderName: String,
    body: String,
    isMine: Boolean = false,
    inReplyTo: InReplyToDetails? = null,
    reactions: ImmutableList<AggregatedReaction> = persistentListOf(),
) = TimelineItem.Event(
    id = UniqueId("sample-$senderName-$body"),
    senderId = if (isMine) SAMPLE_CURRENT_USER_ID else SAMPLE_SENDER_ID,
    senderProfile = ProfileDetails.Ready(
        displayName = senderName,
        displayNameAmbiguous = false,
        avatarUrl = null,
        displayedStatus = null,
    ),
    senderAvatar = AvatarData(SAMPLE_SENDER_ID.value, senderName, size = AvatarSize.TimelineSender),
    content = TimelineItemTextContent(
        body = body,
        htmlDocument = null,
        formattedBody = body,
        isEdited = false,
    ),
    sentTime = "12:34",
    isMine = isMine,
    isEditable = false,
    canBeRepliedTo = false,
    reactionsState = TimelineItemReactions(reactions),
    readReceiptState = TimelineItemReadReceipts(persistentListOf()),
    localSendState = null,
    editSendState = null,
    redactionSendState = null,
    inReplyTo = inReplyTo,
    threadInfo = null,
    origin = null,
    timelineItemDebugInfoProvider = { error("The sample is not interactive") },
    messageShieldProvider = { null },
    sendHandleProvider = { null },
    forwarder = null,
    forwarderProfile = null,
)

private fun aSampleReaction() = AggregatedReaction(
    currentUserId = SAMPLE_CURRENT_USER_ID,
    key = "😁",
    senders = persistentListOf(
        AggregatedReactionSender(senderId = SAMPLE_SENDER_ID, timestamp = 0L, sentTime = "12:34"),
    ),
)

private fun aSampleReply(sender: String, body: String) = InReplyToDetails.Ready(
    eventId = EventId($$"$sampleReplyEventId"),
    senderId = SAMPLE_CURRENT_USER_ID,
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

private val SAMPLE_CURRENT_USER_ID = UserId("@me:element.io")
private val SAMPLE_SENDER_ID = UserId("@sample:element.io")

@PreviewsDayNight
@Composable
internal fun TimelineSamplePreview() = ElementPreview {
    TimelineSample(
        textSizeSp = MessageTextSize.DEFAULT,
        modifier = Modifier.fillMaxSize(),
    )
}
