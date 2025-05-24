/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.notification

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.timeline.item.event.MessageType

data class NotificationData(
    val sessionId: SessionId,
    val eventId: EventId,
    val threadId: ThreadId?,
    val roomId: RoomId,
    // mxc url
    val senderAvatarUrl: String?,
    // private, must use `getDisambiguatedDisplayName`
    private val senderDisplayName: String?,
    private val senderIsNameAmbiguous: Boolean,
    val roomAvatarUrl: String?,
    val roomDisplayName: String?,
    val isDirect: Boolean,
    val isDm: Boolean,
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

        data class RoomRedaction(
            val redactedEventId: EventId?,
            val reason: String?,
        ) : MessageLike

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
            val userId: UserId,
            val membershipState: RoomMembershipState
        ) : StateEvent

        data object RoomName : StateEvent
        data object RoomPinnedEvents : StateEvent
        data object RoomPowerLevels : StateEvent
        data object RoomServerAcl : StateEvent
        data object RoomThirdPartyInvite : StateEvent
        data object RoomTombstone : StateEvent
        data class RoomTopic(val topic: String) : StateEvent
        data object SpaceChild : StateEvent
        data object SpaceParent : StateEvent
    }

    data class Invite(
        val senderId: UserId,
    ) : NotificationContent
}

enum class CallNotifyType {
    RING,
    NOTIFY
}
