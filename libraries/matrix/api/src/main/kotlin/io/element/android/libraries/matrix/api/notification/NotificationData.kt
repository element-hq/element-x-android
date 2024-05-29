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
    // mxc url
    val senderAvatarUrl: String?,
    // private, must use `getDisambiguatedDisplayName`
    private val senderDisplayName: String?,
    private val senderIsNameAmbiguous: Boolean,
    val roomAvatarUrl: String?,
    val roomDisplayName: String?,
    val isDirect: Boolean,
    val isEncrypted: Boolean,
    val isNoisy: Boolean,
    val timestamp: Long,
    val content: NotificationContent,
    val hasMention: Boolean,
) {
    fun getDisambiguatedDisplayName(userId: UserId): String = when {
        senderDisplayName.isNullOrBlank() -> userId.value
        senderIsNameAmbiguous -> "$senderDisplayName ($userId)"
        else -> senderDisplayName
    }
}

sealed interface NotificationContent {
    sealed interface MessageLike : NotificationContent {
        data object CallAnswer : MessageLike
        data class CallInvite(
            val senderId: UserId,
        ) : MessageLike
        data class CallNotify(
            val senderId: UserId,
            val type: CallNotifyType,
        ) : MessageLike

        data object CallHangup : MessageLike
        data object CallCandidates : MessageLike
        data object KeyVerificationReady : MessageLike
        data object KeyVerificationStart : MessageLike
        data object KeyVerificationCancel : MessageLike
        data object KeyVerificationAccept : MessageLike
        data object KeyVerificationKey : MessageLike
        data object KeyVerificationMac : MessageLike
        data object KeyVerificationDone : MessageLike
        data class ReactionContent(
            val relatedEventId: String
        ) : MessageLike

        data object RoomEncrypted : MessageLike
        data class RoomMessage(
            val senderId: UserId,
            val messageType: MessageType
        ) : MessageLike

        data object RoomRedaction : MessageLike
        data object Sticker : MessageLike
        data class Poll(
            val senderId: UserId,
            val question: String,
        ) : MessageLike
    }

    sealed interface StateEvent : NotificationContent {
        data object PolicyRuleRoom : StateEvent
        data object PolicyRuleServer : StateEvent
        data object PolicyRuleUser : StateEvent
        data object RoomAliases : StateEvent
        data object RoomAvatar : StateEvent
        data object RoomCanonicalAlias : StateEvent
        data object RoomCreate : StateEvent
        data object RoomEncryption : StateEvent
        data object RoomGuestAccess : StateEvent
        data object RoomHistoryVisibility : StateEvent
        data object RoomJoinRules : StateEvent
        data class RoomMemberContent(
            val userId: String,
            val membershipState: RoomMembershipState
        ) : StateEvent

        data object RoomName : StateEvent
        data object RoomPinnedEvents : StateEvent
        data object RoomPowerLevels : StateEvent
        data object RoomServerAcl : StateEvent
        data object RoomThirdPartyInvite : StateEvent
        data object RoomTombstone : StateEvent
        data object RoomTopic : StateEvent
        data object SpaceChild : StateEvent
        data object SpaceParent : StateEvent
    }
}

enum class CallNotifyType {
    RING,
    NOTIFY
}
