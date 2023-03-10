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

package io.element.android.libraries.matrix.impl.timeline.item.event

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.event.FailedToParseMessageLike
import io.element.android.libraries.matrix.api.timeline.item.event.FailedToParseState
import io.element.android.libraries.matrix.api.timeline.item.event.MembershipChange
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileChange
import io.element.android.libraries.matrix.api.timeline.item.event.RedactedContent
import io.element.android.libraries.matrix.api.timeline.item.event.RoomMembership
import io.element.android.libraries.matrix.api.timeline.item.event.StickerContent
import io.element.android.libraries.matrix.api.timeline.item.event.TimelineEventContent
import io.element.android.libraries.matrix.api.timeline.item.event.UnknownContent
import io.element.android.libraries.matrix.impl.media.map
import org.matrix.rustcomponents.sdk.TimelineItemContent
import org.matrix.rustcomponents.sdk.TimelineItemContentKind

class TimelineEventContentMapper(private val eventMessageMapper: EventMessageMapper = EventMessageMapper()) {

    fun map(content: TimelineItemContent): TimelineEventContent = content.use {
        when (val kind = content.kind()) {
            is TimelineItemContentKind.FailedToParseMessageLike -> {
                FailedToParseMessageLike(
                    eventType = kind.eventType,
                    error = kind.error
                )
            }
            is TimelineItemContentKind.FailedToParseState -> {
                FailedToParseState(
                    eventType = kind.eventType,
                    stateKey = kind.stateKey,
                    error = kind.error
                )
            }
            TimelineItemContentKind.Message -> {
                val message = content.asMessage()
                if (message == null) {
                    UnknownContent
                } else {
                    eventMessageMapper.map(message)
                }
            }
            is TimelineItemContentKind.ProfileChange -> {
                ProfileChange(
                    displayName = kind.displayName,
                    prevDisplayName = kind.prevDisplayName,
                    avatarUrl = kind.avatarUrl,
                    prevAvatarUrl = kind.prevAvatarUrl
                )
            }
            TimelineItemContentKind.RedactedMessage -> {
                RedactedContent
            }
            is TimelineItemContentKind.RoomMembership -> {
                RoomMembership(
                    UserId(kind.userId),
                    MembershipChange.JOINED
                )
            }
            is TimelineItemContentKind.State -> {
                UnknownContent
            }
            is TimelineItemContentKind.Sticker -> {
                StickerContent(
                    body = kind.body,
                    info = kind.info.map(),
                    url = kind.url
                )
            }
            is TimelineItemContentKind.UnableToDecrypt -> {
                UnknownContent
            }
        }
    }
}
