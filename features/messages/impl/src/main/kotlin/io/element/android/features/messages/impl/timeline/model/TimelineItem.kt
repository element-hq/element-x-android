/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model

import androidx.compose.runtime.Immutable
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStickerContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemVirtualModel
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.SendHandle
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.ThreadSummary
import io.element.android.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import io.element.android.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import io.element.android.libraries.matrix.api.timeline.item.event.MessageShield
import io.element.android.libraries.matrix.api.timeline.item.event.MessageShieldProvider
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileDetails
import io.element.android.libraries.matrix.api.timeline.item.event.SendHandleProvider
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

    data class Virtual(
        val id: UniqueId,
        val model: TimelineItemVirtualModel
    ) : TimelineItem

    data class Event(
        val id: UniqueId,
        // Note: eventId can be null when the event is a local echo
        val eventId: EventId? = null,
        val transactionId: TransactionId? = null,
        val senderId: UserId,
        val senderProfile: ProfileDetails,
        val senderAvatar: AvatarData,
        val content: TimelineItemEventContent,
        val sentTimeMillis: Long = 0L,
        val sentTime: String = "",
        val isMine: Boolean = false,
        val isEditable: Boolean,
        val canBeRepliedTo: Boolean,
        val groupPosition: TimelineItemGroupPosition = TimelineItemGroupPosition.None,
        val reactionsState: TimelineItemReactions,
        val readReceiptState: TimelineItemReadReceipts,
        val localSendState: LocalEventSendState?,
        val inReplyTo: InReplyToDetails?,
        val threadInfo: TimelineItemThreadInfo?,
        val origin: TimelineItemEventOrigin?,
        val timelineItemDebugInfoProvider: TimelineItemDebugInfoProvider,
        val messageShieldProvider: MessageShieldProvider,
        val sendHandleProvider: SendHandleProvider,
    ) : TimelineItem {
        val showSenderInformation = groupPosition.isNew() && !isMine

        val safeSenderName: String = senderProfile.getDisambiguatedDisplayName(senderId)

        val failedToSend: Boolean = localSendState is LocalEventSendState.Failed

        val isTextMessage: Boolean = content is TimelineItemTextBasedContent

        val isSticker: Boolean = content is TimelineItemStickerContent

        val isRemote = eventId != null

        /** Whether a click on any part of the event bubble should trigger the 'onContentClick' callback.
         *
         *  This is `true` for all events except for visual media events with a caption or formatted caption.
         */
        val isWholeContentClickable = when (content) {
            is TimelineItemStickerContent -> content.formattedCaption == null && content.caption == null
            is TimelineItemImageContent -> content.formattedCaption == null && content.caption == null
            is TimelineItemVideoContent -> content.formattedCaption == null && content.caption == null
            else -> true
        }

        val eventOrTransactionId: EventOrTransactionId
            get() = EventOrTransactionId.from(eventId = eventId, transactionId = transactionId)

        // No need to be lazy here?
        val messageShield: MessageShield? = messageShieldProvider(strict = false)

        val debugInfo: TimelineItemDebugInfo
            get() = timelineItemDebugInfoProvider()

        val sendhandle: SendHandle? get() = sendHandleProvider()
    }

    data class GroupedEvents(
        val id: UniqueId,
        val events: ImmutableList<Event>,
        val aggregatedReadReceipts: ImmutableList<ReadReceiptData>,
    ) : TimelineItem
}

sealed interface TimelineItemThreadInfo {
    data class ThreadRoot(val summary: ThreadSummary, val latestEventText: String?) : TimelineItemThreadInfo
    data class ThreadResponse(val threadRootId: ThreadId) : TimelineItemThreadInfo
}
