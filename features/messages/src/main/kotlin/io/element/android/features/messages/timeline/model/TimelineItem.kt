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

package io.element.android.features.messages.timeline.model

import androidx.compose.runtime.Immutable
import io.element.android.features.messages.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.timeline.model.virtual.TimelineItemVirtualModel
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.core.EventId

@Immutable
sealed interface TimelineItem {

    fun identifier(): String = when(this){
        is Event -> id.value
        is Virtual -> id
    }

    @Immutable
    data class Virtual(
        val id: String,
        val model: TimelineItemVirtualModel
    ) : TimelineItem

    @Immutable
    data class Event(
        val id: EventId,
        val eventId: EventId? = null,
        val senderId: String,
        val senderDisplayName: String?,
        val senderAvatar: AvatarData,
        val content: TimelineItemEventContent,
        val sentTime: String = "",
        val isMine: Boolean = false,
        val groupPosition: TimelineItemGroupPosition = TimelineItemGroupPosition.None,
        val reactionsState: TimelineItemReactions
    ) : TimelineItem {

        val showSenderInformation = groupPosition.isNew() && !isMine

        val safeSenderName: String = senderDisplayName ?: senderId
    }
}
