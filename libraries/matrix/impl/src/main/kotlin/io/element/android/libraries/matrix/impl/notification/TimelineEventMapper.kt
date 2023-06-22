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

package io.element.android.libraries.matrix.impl.notification

import io.element.android.libraries.matrix.api.notification.NotificationEvent
import org.matrix.rustcomponents.sdk.MessageLikeEventContent
import org.matrix.rustcomponents.sdk.MessageType
import org.matrix.rustcomponents.sdk.StateEventContent
import org.matrix.rustcomponents.sdk.TimelineEvent
import org.matrix.rustcomponents.sdk.TimelineEventType
import org.matrix.rustcomponents.sdk.use
import javax.inject.Inject

class TimelineEventMapper @Inject constructor() {

    fun map(timelineEvent: TimelineEvent): NotificationEvent {
        return timelineEvent.use {
            NotificationEvent(
                timestamp = it.timestamp().toLong(),
                content = it.eventType().toContent(),
                contentUrl = null // TODO it.eventType().toContentUrl(),
            )
        }
    }
}

private fun TimelineEventType.toContent(): String {
    return when (this) {
        is TimelineEventType.MessageLike -> content.toContent()
        is TimelineEventType.State -> content.toContent()
    }
}

private fun StateEventContent.toContent(): String {
    return when (this) {
        StateEventContent.PolicyRuleRoom -> "PolicyRuleRoom"
        StateEventContent.PolicyRuleServer -> "PolicyRuleServer"
        StateEventContent.PolicyRuleUser -> "PolicyRuleUser"
        StateEventContent.RoomAliases -> "RoomAliases"
        StateEventContent.RoomAvatar -> "RoomAvatar"
        StateEventContent.RoomCanonicalAlias -> "RoomCanonicalAlias"
        StateEventContent.RoomCreate -> "RoomCreate"
        StateEventContent.RoomEncryption -> "RoomEncryption"
        StateEventContent.RoomGuestAccess -> "RoomGuestAccess"
        StateEventContent.RoomHistoryVisibility -> "RoomHistoryVisibility"
        StateEventContent.RoomJoinRules -> "RoomJoinRules"
        is StateEventContent.RoomMemberContent -> "$userId is now $membershipState"
        StateEventContent.RoomName -> "RoomName"
        StateEventContent.RoomPinnedEvents -> "RoomPinnedEvents"
        StateEventContent.RoomPowerLevels -> "RoomPowerLevels"
        StateEventContent.RoomServerAcl -> "RoomServerAcl"
        StateEventContent.RoomThirdPartyInvite -> "RoomThirdPartyInvite"
        StateEventContent.RoomTombstone -> "RoomTombstone"
        StateEventContent.RoomTopic -> "RoomTopic"
        StateEventContent.SpaceChild -> "SpaceChild"
        StateEventContent.SpaceParent -> "SpaceParent"
    }
}

private fun MessageLikeEventContent.toContent(): String {
    return use {
        when (it) {
            MessageLikeEventContent.CallAnswer -> "CallAnswer"
            MessageLikeEventContent.CallCandidates -> "CallCandidates"
            MessageLikeEventContent.CallHangup -> "CallHangup"
            MessageLikeEventContent.CallInvite -> "CallInvite"
            MessageLikeEventContent.KeyVerificationAccept -> "KeyVerificationAccept"
            MessageLikeEventContent.KeyVerificationCancel -> "KeyVerificationCancel"
            MessageLikeEventContent.KeyVerificationDone -> "KeyVerificationDone"
            MessageLikeEventContent.KeyVerificationKey -> "KeyVerificationKey"
            MessageLikeEventContent.KeyVerificationMac -> "KeyVerificationMac"
            MessageLikeEventContent.KeyVerificationReady -> "KeyVerificationReady"
            MessageLikeEventContent.KeyVerificationStart -> "KeyVerificationStart"
            is MessageLikeEventContent.ReactionContent -> "Reacted to ${it.relatedEventId.take(8)}…"
            MessageLikeEventContent.RoomEncrypted -> "RoomEncrypted"
            is MessageLikeEventContent.RoomMessage -> it.messageType.toContent()
            MessageLikeEventContent.RoomRedaction -> "RoomRedaction"
            MessageLikeEventContent.Sticker -> "Sticker"
        }
    }
}

private fun MessageType.toContent(): String {
    return when (this) {
        is MessageType.Audio -> content.use { it.body }
        is MessageType.Emote -> content.body
        is MessageType.File -> content.use { it.body }
        is MessageType.Image -> content.use { it.body }
        is MessageType.Notice -> content.body
        is MessageType.Text -> content.body
        is MessageType.Video -> content.use { it.body }
        is MessageType.Location -> content.body
    }
}
