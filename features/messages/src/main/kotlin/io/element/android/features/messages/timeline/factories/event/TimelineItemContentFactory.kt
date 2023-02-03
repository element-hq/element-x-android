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

package io.element.android.features.messages.timeline.factories.event

import io.element.android.features.messages.timeline.model.event.TimelineItemEventContent
import org.matrix.rustcomponents.sdk.TimelineItemContentKind
import javax.inject.Inject

typealias RustTimelineItemContent = org.matrix.rustcomponents.sdk.TimelineItemContent

class TimelineItemContentFactory @Inject constructor(
    private val messageFactory: TimelineItemContentMessageFactory,
    private val redactedMessageFactory: TimelineItemContentRedactedFactory,
    private val stickerFactory: TimelineItemContentStickerFactory,
    private val utdFactory: TimelineItemContentUTDFactory,
    private val roomMembershipFactory: TimelineItemContentRoomMembershipFactory,
    private val profileChangeFactory: TimelineItemContentProfileChangeFactory,
    private val stateFactory: TimelineItemContentStateFactory,
    private val failedToParseMessageFactory: TimelineItemContentFailedToParseMessageFactory,
    private val failedToParseStateFactory: TimelineItemContentFailedToParseStateFactory
) {

    fun create(itemContent: RustTimelineItemContent): TimelineItemEventContent {
        return when (val kind = itemContent.kind()) {
            is TimelineItemContentKind.Message -> messageFactory.create(itemContent.asMessage())
            is TimelineItemContentKind.RedactedMessage -> redactedMessageFactory.create(kind)
            is TimelineItemContentKind.Sticker -> stickerFactory.create(kind)
            is TimelineItemContentKind.UnableToDecrypt -> utdFactory.create(kind)
            is TimelineItemContentKind.RoomMembership -> roomMembershipFactory.create(kind)
            is TimelineItemContentKind.ProfileChange -> profileChangeFactory.create(kind)
            is TimelineItemContentKind.State -> stateFactory.create(kind)
            is TimelineItemContentKind.FailedToParseMessageLike -> failedToParseMessageFactory.create(kind)
            is TimelineItemContentKind.FailedToParseState -> failedToParseStateFactory.create(kind)
        }
    }
}
