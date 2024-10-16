/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model

import androidx.compose.runtime.Immutable
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStickerContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemVirtualModel
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import io.element.android.libraries.matrix.api.timeline.item.event.MessageShield
import io.element.android.libraries.matrix.api.timeline.item.event.MessageShieldProvider
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileTimelineDetails
import io.element.android.libraries.matrix.api.timeline.item.event.TimelineItemDebugInfoProvider
import io.element.android.libraries.matrix.api.timeline.item.event.TimelineItemEventOrigin
import io.element.android.libraries.matrix.api.timeline.item.event.getDisambiguatedDisplayName
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToDetails
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface TimelineItem {
    fun identifier(): UniqueId = when (this) {
        is Event -> id
        is Virtual -> id
        is GroupedEvents -> id
    }

    fun isEvent(eventId: EventId?): Boolean {
        if (eventId == null) return false
        return when (this) {
            is Event -> this.eventId == eventId
            else -> false
        }
    }

    fun contentType(): String = when (this) {
        is Event -> content.type
        is Virtual -> model.type
        is GroupedEvents -> "groupedEvent"
    }

    @Immutable
    data class Virtual(
        val id: UniqueId,
        val model: TimelineItemVirtualModel
    ) : TimelineItem

    @Immutable
    data class Event(
        val id: UniqueId,
        // Note: eventId can be null when the event is a local echo
        val eventId: EventId? = null,
        val transactionId: TransactionId? = null,
        val senderId: UserId,
        val senderProfile: ProfileTimelineDetails,
        val senderAvatar: AvatarData,
        val content: TimelineItemEventContent,
        val sentTime: String = "",
        val isMine: Boolean = false,
        val isEditable: Boolean,
        val canBeRepliedTo: Boolean,
        val groupPosition: TimelineItemGroupPosition = TimelineItemGroupPosition.None,
        val reactionsState: TimelineItemReactions,
        val readReceiptState: TimelineItemReadReceipts,
        val localSendState: LocalEventSendState?,
        val inReplyTo: InReplyToDetails?,
        val isThreaded: Boolean,
        val origin: TimelineItemEventOrigin?,
        val timelineItemDebugInfoProvider: TimelineItemDebugInfoProvider,
        val messageShieldProvider: MessageShieldProvider,
    ) : TimelineItem {
        val showSenderInformation = groupPosition.isNew() && !isMine

        val safeSenderName: String = senderProfile.getDisambiguatedDisplayName(senderId)

        val failedToSend: Boolean = localSendState is LocalEventSendState.Failed

        val isTextMessage: Boolean = content is TimelineItemTextBasedContent

        val isSticker: Boolean = content is TimelineItemStickerContent

        val isRemote = eventId != null

        // No need to be lazy here?
        val messageShield: MessageShield? = messageShieldProvider(strict = false)

        val debugInfo: TimelineItemDebugInfo
            get() = timelineItemDebugInfoProvider()
    }

    @Immutable
    data class GroupedEvents(
        val id: UniqueId,
        val events: ImmutableList<Event>,
        val aggregatedReadReceipts: ImmutableList<ReadReceiptData>,
    ) : TimelineItem
}
