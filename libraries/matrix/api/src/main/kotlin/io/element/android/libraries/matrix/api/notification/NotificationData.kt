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

package io.element.android.libraries.matrix.api.notification

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.timeline.item.event.MessageType

data class NotificationData(
    val eventId: EventId,
    val roomId: RoomId,
    val senderAvatarUrl: String?,
    val senderDisplayName: String?,
    val roomAvatarUrl: String?,
    val roomDisplayName: String?,
    val isDirect: Boolean,
    val isEncrypted: Boolean,
    val isNoisy: Boolean,
    val timestamp: Long,
    val content: NotificationContent,
    // For images for instance
    val contentUrl: String?,
)

sealed interface NotificationContent {
    sealed interface MessageLike : NotificationContent {
        object CallAnswer : MessageLike
        object CallInvite : MessageLike
        object CallHangup : MessageLike
        object CallCandidates : MessageLike
        object KeyVerificationReady : MessageLike
        object KeyVerificationStart : MessageLike
        object KeyVerificationCancel : MessageLike
        object KeyVerificationAccept : MessageLike
        object KeyVerificationKey : MessageLike
        object KeyVerificationMac : MessageLike
        object KeyVerificationDone : MessageLike
        data class ReactionContent(
            val relatedEventId: String
        ) : MessageLike
        object RoomEncrypted : MessageLike
        data class RoomMessage(
            val senderId: UserId,
            val messageType: MessageType
        ) : MessageLike
        object RoomRedaction : MessageLike
        object Sticker : MessageLike
    }

    sealed interface StateEvent : NotificationContent {
        object PolicyRuleRoom : StateEvent
        object PolicyRuleServer : StateEvent
        object PolicyRuleUser : StateEvent
        object RoomAliases : StateEvent
        object RoomAvatar : StateEvent
        object RoomCanonicalAlias : StateEvent
        object RoomCreate : StateEvent
        object RoomEncryption : StateEvent
        object RoomGuestAccess : StateEvent
        object RoomHistoryVisibility : StateEvent
        object RoomJoinRules : StateEvent
        data class RoomMemberContent(
            val userId: String,
            val membershipState: RoomMembershipState
        ) : StateEvent
        object RoomName : StateEvent
        object RoomPinnedEvents : StateEvent
        object RoomPowerLevels : StateEvent
        object RoomServerAcl : StateEvent
        object RoomThirdPartyInvite : StateEvent
        object RoomTombstone : StateEvent
        object RoomTopic : StateEvent
        object SpaceChild : StateEvent
        object SpaceParent : StateEvent
    }

}
