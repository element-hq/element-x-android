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

package io.element.android.x.matrix.room.message

import io.element.android.x.matrix.core.EventId
import io.element.android.x.matrix.core.UserId
import org.matrix.rustcomponents.sdk.EventTimelineItem

class RoomMessageFactory {
    fun create(eventTimelineItem: EventTimelineItem?): RoomMessage? {
        eventTimelineItem ?: return null
        return RoomMessage(
            eventId = EventId(eventTimelineItem.eventId() ?: ""),
            body = eventTimelineItem.content().asMessage()?.body() ?: "",
            sender = UserId(eventTimelineItem.sender()),
            originServerTs = eventTimelineItem.originServerTs()?.toLong() ?: 0L
        )
    }
}
