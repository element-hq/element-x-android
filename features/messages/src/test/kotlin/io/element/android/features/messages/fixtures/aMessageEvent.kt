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

package io.element.android.features.messages.fixtures

import io.element.android.features.messages.timeline.model.TimelineItem
import io.element.android.features.messages.timeline.model.TimelineItemReactions
import io.element.android.features.messages.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.timeline.model.event.TimelineItemTextContent
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrixtest.AN_EVENT_ID
import io.element.android.libraries.matrixtest.A_MESSAGE
import io.element.android.libraries.matrixtest.A_USER_ID
import io.element.android.libraries.matrixtest.A_USER_NAME
import kotlinx.collections.immutable.persistentListOf

internal fun aMessageEvent(
    isMine: Boolean = true,
    content: TimelineItemEventContent = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null),
) = TimelineItem.Event(
    id = AN_EVENT_ID.value,
    eventId = AN_EVENT_ID,
    senderId = A_USER_ID.value,
    senderDisplayName = A_USER_NAME,
    senderAvatar = AvatarData(A_USER_ID.value, A_USER_NAME),
    content = content,
    sentTime = "",
    isMine = isMine,
    reactionsState = TimelineItemReactions(persistentListOf())
)
