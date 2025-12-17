/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.notification

import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.notification.NotificationContent
import io.element.android.libraries.matrix.api.notification.RtcNotificationType
import io.element.android.libraries.matrix.impl.room.member.RoomMemberMapper
import io.element.android.libraries.matrix.impl.timeline.item.event.EventMessageMapper
import org.matrix.rustcomponents.sdk.MessageLikeEventContent
import org.matrix.rustcomponents.sdk.StateEventContent
import org.matrix.rustcomponents.sdk.TimelineEvent
import org.matrix.rustcomponents.sdk.TimelineEventType
import org.matrix.rustcomponents.sdk.use
import org.matrix.rustcomponents.sdk.RtcNotificationType as SdkRtcNotificationType

class TimelineEventToNotificationContentMapper {
    fun map(timelineEvent: TimelineEvent): Result<NotificationContent> {
        return runCatchingExceptions {
            timelineEvent.use {
                val senderId = UserId(timelineEvent.senderId())
                timelineEvent.eventType().use { eventType ->
                    eventType.toContent(senderId = senderId)
                }
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
            NotificationContent.StateEvent.RoomMemberContent(
                userId = UserId(userId),
                membershipState = RoomMemberMapper.mapMembership(membershipState),
            )
        }
        StateEventContent.RoomName -> NotificationContent.StateEvent.RoomName
        StateEventContent.RoomPinnedEvents -> NotificationContent.StateEvent.RoomPinnedEvents
        StateEventContent.RoomPowerLevels -> NotificationContent.StateEvent.RoomPowerLevels
        StateEventContent.RoomServerAcl -> NotificationContent.StateEvent.RoomServerAcl
        StateEventContent.RoomThirdPartyInvite -> NotificationContent.StateEvent.RoomThirdPartyInvite
        StateEventContent.RoomTombstone -> NotificationContent.StateEvent.RoomTombstone
        is StateEventContent.RoomTopic -> NotificationContent.StateEvent.RoomTopic(topic)
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
            is MessageLikeEventContent.RtcNotification -> NotificationContent.MessageLike.RtcNotification(
                senderId = senderId,
                type = notificationType.map(),
                expirationTimestampMillis = expirationTs.toLong()
            )
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
            is MessageLikeEventContent.RoomRedaction -> NotificationContent.MessageLike.RoomRedaction(
                redactedEventId = redactedEventId?.let(::EventId),
                reason = reason,
            )
            MessageLikeEventContent.Sticker -> NotificationContent.MessageLike.Sticker
            is MessageLikeEventContent.Poll -> NotificationContent.MessageLike.Poll(senderId, question)
        }
    }
}

private fun SdkRtcNotificationType.map(): RtcNotificationType = when (this) {
    SdkRtcNotificationType.NOTIFICATION -> RtcNotificationType.NOTIFY
    SdkRtcNotificationType.RING -> RtcNotificationType.RING
}
