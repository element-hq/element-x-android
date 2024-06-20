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

package io.element.android.features.messages.impl.timeline.model

import androidx.compose.runtime.Immutable
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStickerContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemVirtualModel
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileTimelineDetails
import io.element.android.libraries.matrix.api.timeline.item.event.TimelineItemEventOrigin
import io.element.android.libraries.matrix.api.timeline.item.event.getDisambiguatedDisplayName
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToDetails
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface TimelineItem {
    fun identifier(): String = when (this) {
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
        val id: String,
        val model: TimelineItemVirtualModel
    ) : TimelineItem

    @Immutable
    data class Event(
        val id: String,
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
        val groupPosition: TimelineItemGroupPosition = TimelineItemGroupPosition.None,
        val reactionsState: TimelineItemReactions,
        val readReceiptState: TimelineItemReadReceipts,
        val localSendState: LocalEventSendState?,
        val inReplyTo: InReplyToDetails?,
        val isThreaded: Boolean,
        val debugInfo: TimelineItemDebugInfo,
        val origin: TimelineItemEventOrigin?,
    ) : TimelineItem {
        val showSenderInformation = groupPosition.isNew() && !isMine

        val safeSenderName: String = senderProfile.getDisambiguatedDisplayName(senderId)

        val failedToSend: Boolean = localSendState is LocalEventSendState.SendingFailed

        val isTextMessage: Boolean = content is TimelineItemTextBasedContent

        val isSticker: Boolean = content is TimelineItemStickerContent

        val isRemote = eventId != null
    }

    @Immutable
    data class GroupedEvents(
        val id: String,
        val events: ImmutableList<Event>,
        val aggregatedReadReceipts: ImmutableList<ReadReceiptData>,
    ) : TimelineItem
}
