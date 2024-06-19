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
import io.element.android.libraries.matrix.api.timeline.item.event.CallNotifyContent
import io.element.android.libraries.matrix.api.timeline.item.event.EventContent
import io.element.android.libraries.matrix.api.timeline.item.event.FailedToParseMessageLikeContent
import io.element.android.libraries.matrix.api.timeline.item.event.FailedToParseStateContent
import io.element.android.libraries.matrix.api.timeline.item.event.LegacyCallInviteContent
import io.element.android.libraries.matrix.api.timeline.item.event.MembershipChange
import io.element.android.libraries.matrix.api.timeline.item.event.OtherState
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileChangeContent
import io.element.android.libraries.matrix.api.timeline.item.event.RedactedContent
import io.element.android.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import io.element.android.libraries.matrix.api.timeline.item.event.StateContent
import io.element.android.libraries.matrix.api.timeline.item.event.StickerContent
import io.element.android.libraries.matrix.api.timeline.item.event.UnableToDecryptContent
import io.element.android.libraries.matrix.api.timeline.item.event.UnknownContent
import io.element.android.libraries.matrix.api.timeline.item.event.UtdCause
import io.element.android.libraries.matrix.impl.media.map
import io.element.android.libraries.matrix.impl.poll.map
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import org.matrix.rustcomponents.sdk.TimelineItemContent
import org.matrix.rustcomponents.sdk.TimelineItemContentKind
import org.matrix.rustcomponents.sdk.use
import org.matrix.rustcomponents.sdk.EncryptedMessage as RustEncryptedMessage
import org.matrix.rustcomponents.sdk.MembershipChange as RustMembershipChange
import org.matrix.rustcomponents.sdk.OtherState as RustOtherState
import uniffi.matrix_sdk_crypto.UtdCause as RustUtdCause

class TimelineEventContentMapper(private val eventMessageMapper: EventMessageMapper = EventMessageMapper()) {
    fun map(content: TimelineItemContent): EventContent {
        return content.use {
            content.kind().use { kind ->
                map(content, kind)
            }
        }
    }

    private fun map(content: TimelineItemContent, kind: TimelineItemContentKind) = when (kind) {
        is TimelineItemContentKind.FailedToParseMessageLike -> {
            FailedToParseMessageLikeContent(
                eventType = kind.eventType,
                error = kind.error
            )
        }
        is TimelineItemContentKind.FailedToParseState -> {
            FailedToParseStateContent(
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
            ProfileChangeContent(
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
            RoomMembershipContent(
                userId = UserId(kind.userId),
                userDisplayName = kind.userDisplayName,
                change = kind.change?.map()
            )
        }
        is TimelineItemContentKind.State -> {
            StateContent(
                stateKey = kind.stateKey,
                content = kind.content.map()
            )
        }
        is TimelineItemContentKind.Sticker -> {
            StickerContent(
                body = kind.body,
                info = kind.info.map(),
                source = kind.source.map(),
            )
        }
        is TimelineItemContentKind.Poll -> {
            PollContent(
                question = kind.question,
                kind = kind.kind.map(),
                maxSelections = kind.maxSelections,
                answers = kind.answers.map { answer -> answer.map() }.toImmutableList(),
                votes = kind.votes.mapValues { vote ->
                    vote.value.map { userId -> UserId(userId) }.toImmutableList()
                }.toImmutableMap(),
                endTime = kind.endTime,
                isEdited = kind.hasBeenEdited,
            )
        }
        is TimelineItemContentKind.UnableToDecrypt -> {
            UnableToDecryptContent(
                data = kind.msg.map()
            )
        }
        is TimelineItemContentKind.CallInvite -> LegacyCallInviteContent
        is TimelineItemContentKind.CallNotify -> CallNotifyContent
        else -> UnknownContent
    }
}

private fun RustMembershipChange.map(): MembershipChange {
    return when (this) {
        RustMembershipChange.NONE -> MembershipChange.NONE
        RustMembershipChange.ERROR -> MembershipChange.ERROR
        RustMembershipChange.JOINED -> MembershipChange.JOINED
        RustMembershipChange.LEFT -> MembershipChange.LEFT
        RustMembershipChange.BANNED -> MembershipChange.BANNED
        RustMembershipChange.UNBANNED -> MembershipChange.UNBANNED
        RustMembershipChange.KICKED -> MembershipChange.KICKED
        RustMembershipChange.INVITED -> MembershipChange.INVITED
        RustMembershipChange.KICKED_AND_BANNED -> MembershipChange.KICKED_AND_BANNED
        RustMembershipChange.INVITATION_ACCEPTED -> MembershipChange.INVITATION_ACCEPTED
        RustMembershipChange.INVITATION_REJECTED -> MembershipChange.INVITATION_REJECTED
        RustMembershipChange.INVITATION_REVOKED -> MembershipChange.INVITATION_REVOKED
        RustMembershipChange.KNOCKED -> MembershipChange.KNOCKED
        RustMembershipChange.KNOCK_ACCEPTED -> MembershipChange.KNOCK_ACCEPTED
        RustMembershipChange.KNOCK_RETRACTED -> MembershipChange.KNOCK_RETRACTED
        RustMembershipChange.KNOCK_DENIED -> MembershipChange.KNOCK_DENIED
        RustMembershipChange.NOT_IMPLEMENTED -> MembershipChange.NOT_IMPLEMENTED
    }
}

private fun RustUtdCause.map(): UtdCause {
    return when (this) {
        RustUtdCause.MEMBERSHIP -> UtdCause.Membership
        RustUtdCause.UNKNOWN -> UtdCause.Unknown
    }
}

// TODO extract state events?
private fun RustOtherState.map(): OtherState {
    return when (this) {
        is RustOtherState.Custom -> OtherState.Custom(eventType)
        RustOtherState.PolicyRuleRoom -> OtherState.PolicyRuleRoom
        RustOtherState.PolicyRuleServer -> OtherState.PolicyRuleServer
        RustOtherState.PolicyRuleUser -> OtherState.PolicyRuleUser
        RustOtherState.RoomAliases -> OtherState.RoomAliases
        is RustOtherState.RoomAvatar -> OtherState.RoomAvatar(url)
        RustOtherState.RoomCanonicalAlias -> OtherState.RoomCanonicalAlias
        RustOtherState.RoomCreate -> OtherState.RoomCreate
        RustOtherState.RoomEncryption -> OtherState.RoomEncryption
        RustOtherState.RoomGuestAccess -> OtherState.RoomGuestAccess
        RustOtherState.RoomHistoryVisibility -> OtherState.RoomHistoryVisibility
        RustOtherState.RoomJoinRules -> OtherState.RoomJoinRules
        is RustOtherState.RoomName -> OtherState.RoomName(name)
        RustOtherState.RoomPinnedEvents -> OtherState.RoomPinnedEvents
        is RustOtherState.RoomPowerLevels -> OtherState.RoomUserPowerLevels(users)
        RustOtherState.RoomServerAcl -> OtherState.RoomServerAcl
        is RustOtherState.RoomThirdPartyInvite -> OtherState.RoomThirdPartyInvite(displayName)
        RustOtherState.RoomTombstone -> OtherState.RoomTombstone
        is RustOtherState.RoomTopic -> OtherState.RoomTopic(topic)
        RustOtherState.SpaceChild -> OtherState.SpaceChild
        RustOtherState.SpaceParent -> OtherState.SpaceParent
    }
}

private fun RustEncryptedMessage.map(): UnableToDecryptContent.Data {
    return when (this) {
        is RustEncryptedMessage.MegolmV1AesSha2 -> UnableToDecryptContent.Data.MegolmV1AesSha2(sessionId, cause.map())
        is RustEncryptedMessage.OlmV1Curve25519AesSha2 -> UnableToDecryptContent.Data.OlmV1Curve25519AesSha2(senderKey)
        RustEncryptedMessage.Unknown -> UnableToDecryptContent.Data.Unknown
    }
}
