/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.matrix.impl.timeline.reply

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.event.InReplyTo
import io.element.android.libraries.matrix.impl.timeline.item.event.TimelineEventContentMapper
import io.element.android.libraries.matrix.impl.timeline.item.event.map
import org.matrix.rustcomponents.sdk.InReplyToDetails
import org.matrix.rustcomponents.sdk.RepliedToEventDetails

class InReplyToMapper(
    private val timelineEventContentMapper: TimelineEventContentMapper,
) {

    fun map(inReplyToDetails: InReplyToDetails): InReplyTo {
        val inReplyToId = EventId(inReplyToDetails.eventId)
        return when (val event = inReplyToDetails.event) {
            is RepliedToEventDetails.Ready -> {
                InReplyTo.Ready(
                    eventId = inReplyToId,
                    content = timelineEventContentMapper.map(event.content),
                    senderId = UserId(event.sender),
                    senderProfile = event.senderProfile.map(),
                )
            }
            is RepliedToEventDetails.Error -> InReplyTo.Error(
                eventId = inReplyToId,
                message = event.message,
            )
            RepliedToEventDetails.Pending -> InReplyTo.Pending(
                eventId = inReplyToId,
            )
            is RepliedToEventDetails.Unavailable -> InReplyTo.NotLoaded(
                eventId = inReplyToId
            )
        }
    }
}
