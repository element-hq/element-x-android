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

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.notification.NotificationContent
import io.element.android.libraries.matrix.impl.room.member.RoomMemberMapper
import io.element.android.libraries.matrix.impl.timeline.item.event.EventMessageMapper
import org.matrix.rustcomponents.sdk.MessageLikeEventContent
import org.matrix.rustcomponents.sdk.StateEventContent
import org.matrix.rustcomponents.sdk.TimelineEvent
import org.matrix.rustcomponents.sdk.TimelineEventType
import org.matrix.rustcomponents.sdk.use
import javax.inject.Inject

class TimelineEventToNotificationContentMapper @Inject constructor() {
    fun map(timelineEvent: TimelineEvent): NotificationContent {
        return timelineEvent.use {
            timelineEvent.eventType().use { eventType ->
                eventType.toContent(senderId = UserId(timelineEvent.senderId()))
            }
        }
    }
}

private fun TimelineEventType.toContent(senderId: UserId): NotificationContent {
    return when (this) {
        is TimelineEventType.MessageLike -> content.toContent(senderId)
        is TimelineEventType.State -> content.toContent()
    }
}

private fun StateEventContent.toContent(): NotificationContent.StateEvent {
    return when (this) {
        StateEventContent.PolicyRuleRoom -> NotificationContent.StateEvent.PolicyRuleRoom
        StateEventContent.PolicyRuleServer -> NotificationContent.StateEvent.PolicyRuleServer
        StateEventContent.PolicyRuleUser -> NotificationContent.StateEvent.PolicyRuleUser
        StateEventContent.RoomAliases -> NotificationContent.StateEvent.RoomAliases
        StateEventContent.RoomAvatar -> NotificationContent.StateEvent.RoomAvatar
        StateEventContent.RoomCanonicalAlias -> NotificationContent.StateEvent.RoomCanonicalAlias
        StateEventContent.RoomCreate -> NotificationContent.StateEvent.RoomCreate
        StateEventContent.RoomEncryption -> NotificationContent.StateEvent.RoomEncryption
        StateEventContent.RoomGuestAccess -> NotificationContent.StateEvent.RoomGuestAccess
        StateEventContent.RoomHistoryVisibility -> NotificationContent.StateEvent.RoomHistoryVisibility
        StateEventContent.RoomJoinRules -> NotificationContent.StateEvent.RoomJoinRules
        is StateEventContent.RoomMemberContent -> {
            NotificationContent.StateEvent.RoomMemberContent(userId, RoomMemberMapper.mapMembership(membershipState))
        }
        StateEventContent.RoomName -> NotificationContent.StateEvent.RoomName
        StateEventContent.RoomPinnedEvents -> NotificationContent.StateEvent.RoomPinnedEvents
        StateEventContent.RoomPowerLevels -> NotificationContent.StateEvent.RoomPowerLevels
        StateEventContent.RoomServerAcl -> NotificationContent.StateEvent.RoomServerAcl
        StateEventContent.RoomThirdPartyInvite -> NotificationContent.StateEvent.RoomThirdPartyInvite
        StateEventContent.RoomTombstone -> NotificationContent.StateEvent.RoomTombstone
        StateEventContent.RoomTopic -> NotificationContent.StateEvent.RoomTopic
        StateEventContent.SpaceChild -> NotificationContent.StateEvent.SpaceChild
        StateEventContent.SpaceParent -> NotificationContent.StateEvent.SpaceParent
    }
}

private fun MessageLikeEventContent.toContent(senderId: UserId): NotificationContent.MessageLike {
    return use {
        when (this) {
            MessageLikeEventContent.CallAnswer -> NotificationContent.MessageLike.CallAnswer
            MessageLikeEventContent.CallCandidates -> NotificationContent.MessageLike.CallCandidates
            MessageLikeEventContent.CallHangup -> NotificationContent.MessageLike.CallHangup
            MessageLikeEventContent.CallInvite -> NotificationContent.MessageLike.CallInvite(senderId)
            MessageLikeEventContent.KeyVerificationAccept -> NotificationContent.MessageLike.KeyVerificationAccept
            MessageLikeEventContent.KeyVerificationCancel -> NotificationContent.MessageLike.KeyVerificationCancel
            MessageLikeEventContent.KeyVerificationDone -> NotificationContent.MessageLike.KeyVerificationDone
            MessageLikeEventContent.KeyVerificationKey -> NotificationContent.MessageLike.KeyVerificationKey
            MessageLikeEventContent.KeyVerificationMac -> NotificationContent.MessageLike.KeyVerificationMac
            MessageLikeEventContent.KeyVerificationReady -> NotificationContent.MessageLike.KeyVerificationReady
            MessageLikeEventContent.KeyVerificationStart -> NotificationContent.MessageLike.KeyVerificationStart
            is MessageLikeEventContent.ReactionContent -> NotificationContent.MessageLike.ReactionContent(relatedEventId)
            MessageLikeEventContent.RoomEncrypted -> NotificationContent.MessageLike.RoomEncrypted
            is MessageLikeEventContent.RoomMessage -> {
                NotificationContent.MessageLike.RoomMessage(senderId, EventMessageMapper().mapMessageType(messageType))
            }
            MessageLikeEventContent.RoomRedaction -> NotificationContent.MessageLike.RoomRedaction
            MessageLikeEventContent.Sticker -> NotificationContent.MessageLike.Sticker
            is MessageLikeEventContent.Poll -> NotificationContent.MessageLike.Poll(senderId, question)
        }
    }
}
