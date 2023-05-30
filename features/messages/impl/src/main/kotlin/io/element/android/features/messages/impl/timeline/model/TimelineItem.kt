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
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemVirtualModel
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.event.EventSendState
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface TimelineItem {

    fun identifier(): String = when (this) {
        is Event -> id
        is Virtual -> id
        is GroupedEvents -> id
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
        val eventId: EventId? = null,
        val senderId: UserId,
        val senderDisplayName: String?,
        val senderAvatar: AvatarData,
        val content: TimelineItemEventContent,
        val sentTime: String = "",
        val isMine: Boolean = false,
        val groupPosition: TimelineItemGroupPosition = TimelineItemGroupPosition.None,
        val reactionsState: TimelineItemReactions,
        val sendState: EventSendState,
    ) : TimelineItem {

        val showSenderInformation = groupPosition.isNew() && !isMine

        val safeSenderName: String = senderDisplayName ?: senderId.value
    }

    @Immutable
    data class GroupedEvents(
        val events: ImmutableList<Event>,
    ) : TimelineItem {
        // use last id with a suffix. Last will not change in cas of new event from backpagination.
        val id = events.last().id + "_group"
    }
}
