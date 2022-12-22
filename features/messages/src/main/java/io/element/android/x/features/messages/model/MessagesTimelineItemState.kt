/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.x.features.messages.model

import io.element.android.x.designsystem.components.avatar.AvatarData
import io.element.android.x.features.messages.model.content.MessagesTimelineItemContent

sealed interface MessagesTimelineItemState {
    data class Virtual(
        val id: String
    ) : MessagesTimelineItemState

    data class MessageEvent(
        val id: String,
        val senderId: String,
        val senderDisplayName: String?,
        val senderAvatar: AvatarData,
        val content: MessagesTimelineItemContent,
        val sentTime: String = "",
        val isMine: Boolean = false,
        val groupPosition: MessagesItemGroupPosition = MessagesItemGroupPosition.None,
        val reactionsState: MessagesItemReactionState
    ) : MessagesTimelineItemState {

        val showSenderInformation = groupPosition.isNew() && !isMine

        val safeSenderName: String = senderDisplayName ?: senderId
    }
}
