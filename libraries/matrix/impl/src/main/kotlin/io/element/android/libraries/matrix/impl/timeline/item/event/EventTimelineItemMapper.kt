/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline.item.event

import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import io.element.android.libraries.matrix.api.timeline.item.event.EventDebugInfoProvider
import io.element.android.libraries.matrix.api.timeline.item.event.EventReaction
import io.element.android.libraries.matrix.api.timeline.item.event.EventShieldsProvider
import io.element.android.libraries.matrix.api.timeline.item.event.EventTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import io.element.android.libraries.matrix.api.timeline.item.event.MessageShield
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileTimelineDetails
import io.element.android.libraries.matrix.api.timeline.item.event.ReactionSender
import io.element.android.libraries.matrix.api.timeline.item.event.Receipt
import io.element.android.libraries.matrix.api.timeline.item.event.TimelineItemEventOrigin
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.matrix.rustcomponents.sdk.EventOrTransactionId
import org.matrix.rustcomponents.sdk.EventSendState
import org.matrix.rustcomponents.sdk.EventTimelineItemDebugInfoProvider
import org.matrix.rustcomponents.sdk.Reaction
import org.matrix.rustcomponents.sdk.ShieldState
import uniffi.matrix_sdk_common.ShieldStateCode
import org.matrix.rustcomponents.sdk.EventSendState as RustEventSendState
import org.matrix.rustcomponents.sdk.EventShieldsProvider as RustEventShieldsProvider
import org.matrix.rustcomponents.sdk.EventTimelineItem as RustEventTimelineItem
import org.matrix.rustcomponents.sdk.EventTimelineItemDebugInfo as RustEventTimelineItemDebugInfo
import org.matrix.rustcomponents.sdk.ProfileDetails as RustProfileDetails
import org.matrix.rustcomponents.sdk.Receipt as RustReceipt
import uniffi.matrix_sdk_ui.EventItemOrigin as RustEventItemOrigin

class EventTimelineItemMapper(
    private val contentMapper: TimelineEventContentMapper = TimelineEventContentMapper(),
) {
    fun map(eventTimelineItem: RustEventTimelineItem): EventTimelineItem = eventTimelineItem.run {
        EventTimelineItem(
            eventId = eventOrTransactionId.eventId(),
            transactionId = eventOrTransactionId.transactionId(),
            isEditable = isEditable,
            canBeRepliedTo = canBeRepliedTo,
            isLocal = isLocal,
            isOwn = isOwn,
            isRemote = isRemote,
            localSendState = localSendState?.map(),
            reactions = reactions.map(),
            receipts = readReceipts.map(),
            sender = UserId(sender),
            senderProfile = senderProfile.map(),
            timestamp = timestamp.toLong(),
            content = contentMapper.map(content),
            debugInfoProvider = RustEventDebugInfoProvider(debugInfoProvider),
            origin = origin?.map(),
            messageShieldProvider = RustEventShieldsProvider(shieldsProvider)
        )
    }
}

fun RustProfileDetails.map(): ProfileTimelineDetails {
    return when (this) {
        RustProfileDetails.Pending -> ProfileTimelineDetails.Pending
        RustProfileDetails.Unavailable -> ProfileTimelineDetails.Unavailable
        is RustProfileDetails.Error -> ProfileTimelineDetails.Error(message)
        is RustProfileDetails.Ready -> ProfileTimelineDetails.Ready(
            displayName = displayName,
            displayNameAmbiguous = displayNameAmbiguous,
            avatarUrl = avatarUrl
        )
    }
}

fun RustEventSendState?.map(): LocalEventSendState? {
    return when (this) {
        null -> null
        RustEventSendState.NotSentYet -> LocalEventSendState.Sending
        is RustEventSendState.SendingFailed -> {
            if (isRecoverable) {
                LocalEventSendState.Sending
            } else {
                LocalEventSendState.Failed.Unknown(error)
            }
        }
        is RustEventSendState.Sent -> LocalEventSendState.Sent(EventId(eventId))
        is RustEventSendState.VerifiedUserChangedIdentity -> {
            LocalEventSendState.Failed.VerifiedUserChangedIdentity(users.map { UserId(it) })
        }
        is RustEventSendState.VerifiedUserHasUnsignedDevice -> {
            LocalEventSendState.Failed.VerifiedUserHasUnsignedDevice(
                devices = devices.entries.associate { entry ->
                    UserId(entry.key) to entry.value.map { DeviceId(it) }
                }
            )
        }
        EventSendState.CrossSigningNotSetup -> LocalEventSendState.Failed.CrossSigningNotSetup
        EventSendState.SendingFromUnverifiedDevice -> LocalEventSendState.Failed.SendingFromUnverifiedDevice
    }
}

private fun List<Reaction>?.map(): ImmutableList<EventReaction> {
    return this?.map {
        EventReaction(
            key = it.key,
            senders = it.senders.map { sender ->
                ReactionSender(
                    senderId = UserId(sender.senderId),
                    timestamp = sender.timestamp.toLong()
                )
            }.toImmutableList()
        )
    }?.toImmutableList() ?: persistentListOf()
}

private fun Map<String, RustReceipt>.map(): ImmutableList<Receipt> {
    return map {
        Receipt(
            userId = UserId(it.key),
            timestamp = it.value.timestamp?.toLong() ?: 0
        )
    }
        .sortedByDescending { it.timestamp }
        .toImmutableList()
}

private fun RustEventTimelineItemDebugInfo.map(): TimelineItemDebugInfo {
    return TimelineItemDebugInfo(
        model = model,
        originalJson = originalJson,
        latestEditedJson = latestEditJson,
    )
}

private fun RustEventItemOrigin.map(): TimelineItemEventOrigin {
    return when (this) {
        RustEventItemOrigin.LOCAL -> TimelineItemEventOrigin.LOCAL
        RustEventItemOrigin.SYNC -> TimelineItemEventOrigin.SYNC
        RustEventItemOrigin.PAGINATION -> TimelineItemEventOrigin.PAGINATION
    }
}

private fun ShieldState?.map(): MessageShield? {
    this ?: return null
    val shieldStateCode = when (this) {
        is ShieldState.Grey -> code
        is ShieldState.Red -> code
        ShieldState.None -> null
    } ?: return null
    val isCritical = when (this) {
        ShieldState.None,
        is ShieldState.Grey -> false
        is ShieldState.Red -> true
    }
    return when (shieldStateCode) {
        ShieldStateCode.AUTHENTICITY_NOT_GUARANTEED -> MessageShield.AuthenticityNotGuaranteed(isCritical)
        ShieldStateCode.UNKNOWN_DEVICE -> MessageShield.UnknownDevice(isCritical)
        ShieldStateCode.UNSIGNED_DEVICE -> MessageShield.UnsignedDevice(isCritical)
        ShieldStateCode.UNVERIFIED_IDENTITY -> MessageShield.UnverifiedIdentity(isCritical)
        ShieldStateCode.SENT_IN_CLEAR -> MessageShield.SentInClear(isCritical)
        ShieldStateCode.VERIFICATION_VIOLATION -> MessageShield.VerificationViolation(isCritical)
    }
}

class RustEventDebugInfoProvider(private val debugInfoProvider: EventTimelineItemDebugInfoProvider) : EventDebugInfoProvider {
    override fun get(): TimelineItemDebugInfo {
        return debugInfoProvider.get().map()
    }
}

class RustEventShieldsProvider(private val shieldsProvider: RustEventShieldsProvider) : EventShieldsProvider {
    override fun getShield(strict: Boolean): MessageShield? {
        return shieldsProvider.getShields(strict)?.map()
    }
}

private fun EventOrTransactionId.eventId(): EventId? {
    return (this as? EventOrTransactionId.EventId)?.let { EventId(it.eventId) }
}

private fun EventOrTransactionId.transactionId(): TransactionId? {
    return (this as? EventOrTransactionId.TransactionId)?.let { TransactionId(it.transactionId) }
}
