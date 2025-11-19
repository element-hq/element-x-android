/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline.item.event

import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.EmbeddedEventInfo
import io.element.android.libraries.matrix.api.timeline.item.EventThreadInfo
import io.element.android.libraries.matrix.api.timeline.item.ThreadSummary
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
import io.element.android.libraries.matrix.impl.room.join.map
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import org.matrix.rustcomponents.sdk.EmbeddedEventDetails
import org.matrix.rustcomponents.sdk.MsgLikeKind
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
                is TimelineItemContent.MsgLike -> {
                    when (val kind = it.content.kind) {
                        is MsgLikeKind.Message -> {
                            val inReplyTo = it.content.inReplyTo
                            val threadSummary = it.content.threadSummary?.use { summary ->
                                val numberOfReplies = summary.numReplies().toLong()
                                val latestEvent = summary.latestEvent()
                                val details = when (latestEvent) {
                                    is EmbeddedEventDetails.Unavailable -> AsyncData.Uninitialized
                                    is EmbeddedEventDetails.Pending -> AsyncData.Loading()
                                    is EmbeddedEventDetails.Error -> AsyncData.Failure(IllegalStateException(latestEvent.message))
                                    is EmbeddedEventDetails.Ready -> {
                                        AsyncData.Success(
                                            EmbeddedEventInfo(
                                                eventOrTransactionId = latestEvent.eventOrTransactionId.map(),
                                                content = map(latestEvent.content),
                                                senderId = UserId(latestEvent.sender),
                                                senderProfile = latestEvent.senderProfile.map(),
                                                timestamp = latestEvent.timestamp.toLong(),
                                            )
                                        )
                                    }
                                }
                                ThreadSummary(
                                    latestEvent = details,
                                    numberOfReplies = numberOfReplies,
                                )
                            }
                            val threadRootId = it.content.threadRoot?.let(::ThreadId)
                            val threadInfo = when {
                                threadSummary != null -> EventThreadInfo.ThreadRoot(threadSummary)
                                threadRootId != null -> EventThreadInfo.ThreadResponse(threadRootId)
                                else -> null
                            }
                            eventMessageMapper.map(kind, inReplyTo, threadInfo)
                        }
                        is MsgLikeKind.Redacted -> {
                            RedactedContent
                        }
                        is MsgLikeKind.Poll -> {
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
                        is MsgLikeKind.UnableToDecrypt -> {
                            UnableToDecryptContent(
                                data = kind.msg.map()
                            )
                        }
                        is MsgLikeKind.Sticker -> {
                            StickerContent(
                                filename = kind.body,
                                body = null,
                                info = kind.info.map(),
                                source = kind.source.map(),
                            )
                        }
                        is MsgLikeKind.Other -> UnknownContent
                    }
                }
                is TimelineItemContent.ProfileChange -> {
                    ProfileChangeContent(
                        displayName = it.displayName,
                        prevDisplayName = it.prevDisplayName,
                        avatarUrl = it.avatarUrl,
                        prevAvatarUrl = it.prevAvatarUrl
                    )
                }
                is TimelineItemContent.RoomMembership -> {
                    RoomMembershipContent(
                        userId = UserId(it.userId),
                        userDisplayName = it.userDisplayName,
                        change = it.change?.map(),
                        reason = it.reason,
                    )
                }
                is TimelineItemContent.State -> {
                    StateContent(
                        stateKey = it.stateKey,
                        content = it.content.map()
                    )
                }
                is TimelineItemContent.CallInvite -> LegacyCallInviteContent
                is TimelineItemContent.RtcNotification -> CallNotifyContent
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
        RustUtdCause.SENT_BEFORE_WE_JOINED -> UtdCause.SentBeforeWeJoined
        RustUtdCause.UNKNOWN -> UtdCause.Unknown
        RustUtdCause.VERIFICATION_VIOLATION -> UtdCause.VerificationViolation
        RustUtdCause.UNSIGNED_DEVICE -> UtdCause.UnsignedDevice
        RustUtdCause.UNKNOWN_DEVICE -> UtdCause.UnknownDevice
        RustUtdCause.HISTORICAL_MESSAGE_AND_BACKUP_IS_DISABLED -> UtdCause.HistoricalMessageAndBackupIsDisabled
        RustUtdCause.HISTORICAL_MESSAGE_AND_DEVICE_IS_UNVERIFIED -> UtdCause.HistoricalMessageAndDeviceIsUnverified
        RustUtdCause.WITHHELD_FOR_UNVERIFIED_OR_INSECURE_DEVICE -> UtdCause.WithheldUnverifiedOrInsecureDevice
        RustUtdCause.WITHHELD_BY_SENDER -> UtdCause.WithheldBySender
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
        is RustOtherState.RoomJoinRules -> OtherState.RoomJoinRules(joinRule?.map())
        is RustOtherState.RoomName -> OtherState.RoomName(name)
        is RustOtherState.RoomPinnedEvents -> OtherState.RoomPinnedEvents(change.map())
        is RustOtherState.RoomPowerLevels -> OtherState.RoomUserPowerLevels(users)
        RustOtherState.RoomServerAcl -> OtherState.RoomServerAcl
        is RustOtherState.RoomThirdPartyInvite -> OtherState.RoomThirdPartyInvite(displayName)
        RustOtherState.RoomTombstone -> OtherState.RoomTombstone
        is RustOtherState.RoomTopic -> OtherState.RoomTopic(topic)
        RustOtherState.SpaceChild -> OtherState.SpaceChild
        RustOtherState.SpaceParent -> OtherState.SpaceParent
        is RustOtherState.RoomCreate -> OtherState.RoomCreate
        is RustOtherState.RoomHistoryVisibility -> OtherState.RoomHistoryVisibility
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
