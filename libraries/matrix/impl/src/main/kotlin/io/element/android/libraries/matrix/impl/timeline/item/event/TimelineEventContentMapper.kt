/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
import org.matrix.rustcomponents.sdk.use
import uniffi.matrix_sdk_ui.RoomPinnedEventsChange
import org.matrix.rustcomponents.sdk.EncryptedMessage as RustEncryptedMessage
import org.matrix.rustcomponents.sdk.MembershipChange as RustMembershipChange
import org.matrix.rustcomponents.sdk.OtherState as RustOtherState
import uniffi.matrix_sdk_crypto.UtdCause as RustUtdCause

class TimelineEventContentMapper(
    private val eventMessageMapper: EventMessageMapper = EventMessageMapper(),
) {
    fun map(content: TimelineItemContent): EventContent {
        return content.use {
            when (it) {
                is TimelineItemContent.FailedToParseMessageLike -> {
                    FailedToParseMessageLikeContent(
                        eventType = it.eventType,
                        error = it.error
                    )
                }
                is TimelineItemContent.FailedToParseState -> {
                    FailedToParseStateContent(
                        eventType = it.eventType,
                        stateKey = it.stateKey,
                        error = it.error
                    )
                }
                is TimelineItemContent.Message -> {
                    eventMessageMapper.map(it.content)
                }
                is TimelineItemContent.ProfileChange -> {
                    ProfileChangeContent(
                        displayName = it.displayName,
                        prevDisplayName = it.prevDisplayName,
                        avatarUrl = it.avatarUrl,
                        prevAvatarUrl = it.prevAvatarUrl
                    )
                }
                TimelineItemContent.RedactedMessage -> {
                    RedactedContent
                }
                is TimelineItemContent.RoomMembership -> {
                    RoomMembershipContent(
                        userId = UserId(it.userId),
                        userDisplayName = it.userDisplayName,
                        change = it.change?.map()
                    )
                }
                is TimelineItemContent.State -> {
                    StateContent(
                        stateKey = it.stateKey,
                        content = it.content.map()
                    )
                }
                is TimelineItemContent.Sticker -> {
                    StickerContent(
                        filename = it.body,
                        body = null,
                        info = it.info.map(),
                        source = it.source.map(),
                    )
                }
                is TimelineItemContent.Poll -> {
                    PollContent(
                        question = it.question,
                        kind = it.kind.map(),
                        maxSelections = it.maxSelections,
                        answers = it.answers.map { answer -> answer.map() }.toImmutableList(),
                        votes = it.votes.mapValues { vote ->
                            vote.value.map { userId -> UserId(userId) }.toImmutableList()
                        }.toImmutableMap(),
                        endTime = it.endTime,
                        isEdited = it.hasBeenEdited,
                    )
                }
                is TimelineItemContent.UnableToDecrypt -> {
                    UnableToDecryptContent(
                        data = it.msg.map()
                    )
                }
                is TimelineItemContent.CallInvite -> LegacyCallInviteContent
                is TimelineItemContent.CallNotify -> CallNotifyContent
                else -> UnknownContent
            }
        }
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
        is RustOtherState.RoomPinnedEvents -> OtherState.RoomPinnedEvents(change.map())
        is RustOtherState.RoomPowerLevels -> OtherState.RoomUserPowerLevels(users)
        RustOtherState.RoomServerAcl -> OtherState.RoomServerAcl
        is RustOtherState.RoomThirdPartyInvite -> OtherState.RoomThirdPartyInvite(displayName)
        RustOtherState.RoomTombstone -> OtherState.RoomTombstone
        is RustOtherState.RoomTopic -> OtherState.RoomTopic(topic)
        RustOtherState.SpaceChild -> OtherState.SpaceChild
        RustOtherState.SpaceParent -> OtherState.SpaceParent
    }
}

private fun RoomPinnedEventsChange.map(): OtherState.RoomPinnedEvents.Change {
    return when (this) {
        RoomPinnedEventsChange.ADDED -> OtherState.RoomPinnedEvents.Change.ADDED
        RoomPinnedEventsChange.REMOVED -> OtherState.RoomPinnedEvents.Change.REMOVED
        RoomPinnedEventsChange.CHANGED -> OtherState.RoomPinnedEvents.Change.CHANGED
    }
}

private fun RustEncryptedMessage.map(): UnableToDecryptContent.Data {
    return when (this) {
        is RustEncryptedMessage.MegolmV1AesSha2 -> UnableToDecryptContent.Data.MegolmV1AesSha2(sessionId, cause.map())
        is RustEncryptedMessage.OlmV1Curve25519AesSha2 -> UnableToDecryptContent.Data.OlmV1Curve25519AesSha2(senderKey)
        RustEncryptedMessage.Unknown -> UnableToDecryptContent.Data.Unknown
    }
}
