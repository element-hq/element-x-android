/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline.item.event

import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import io.element.android.libraries.matrix.api.timeline.item.event.EventReaction
import io.element.android.libraries.matrix.api.timeline.item.event.EventTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import io.element.android.libraries.matrix.api.timeline.item.event.MessageShield
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileDetails
import io.element.android.libraries.matrix.api.timeline.item.event.ReactionSender
import io.element.android.libraries.matrix.api.timeline.item.event.Receipt
import io.element.android.libraries.matrix.api.timeline.item.event.TimelineItemEventOrigin
import io.element.android.libraries.matrix.impl.core.RustSendHandle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.matrix.rustcomponents.sdk.EventOrTransactionId
import org.matrix.rustcomponents.sdk.QueueWedgeError
import org.matrix.rustcomponents.sdk.Reaction
import org.matrix.rustcomponents.sdk.ShieldState
import org.matrix.rustcomponents.sdk.TimelineItemContent
import uniffi.matrix_sdk_common.ShieldStateCode
import org.matrix.rustcomponents.sdk.EventSendState as RustEventSendState
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
            isOwn = isOwn,
            isRemote = isRemote,
            localSendState = localSendState?.map(),
            reactions = (content as? TimelineItemContent.MsgLike)?.content?.reactions.map(),
            receipts = readReceipts.map(),
            sender = UserId(sender),
            senderProfile = senderProfile.map(),
            timestamp = timestamp.toLong(),
            content = contentMapper.map(content),
            origin = origin?.map(),
            timelineItemDebugInfoProvider = { lazyProvider.debugInfo().map() },
            messageShieldProvider = { strict -> lazyProvider.getShields(strict)?.map() },
            sendHandleProvider = { lazyProvider.getSendHandle()?.let(::RustSendHandle) }
        )
    }
}

fun RustProfileDetails.map(): ProfileDetails {
    return when (this) {
        RustProfileDetails.Pending -> ProfileDetails.Pending
        RustProfileDetails.Unavailable -> ProfileDetails.Unavailable
        is RustProfileDetails.Error -> ProfileDetails.Error(message)
        is RustProfileDetails.Ready -> ProfileDetails.Ready(
            displayName = displayName,
            displayNameAmbiguous = displayNameAmbiguous,
            avatarUrl = avatarUrl
        )
    }
}

fun RustEventSendState?.map(): LocalEventSendState? {
    return when (this) {
        null -> null
        is RustEventSendState.NotSentYet -> {
            val mediaUploadProgress = this.progress
            if (mediaUploadProgress != null) {
                LocalEventSendState.Sending.MediaWithProgress(
                    index = mediaUploadProgress.index.toLong(),
                    progress = mediaUploadProgress.progress.current.toLong(),
                    total = mediaUploadProgress.progress.total.toLong(),
                )
            } else {
                LocalEventSendState.Sending.Event
            }
        }
        is RustEventSendState.SendingFailed -> {
            when (val queueWedgeError = error) {
                QueueWedgeError.CrossVerificationRequired -> {
                    // The current device is not cross-signed (or cross signing is not setup)
                    LocalEventSendState.Failed.SendingFromUnverifiedDevice
                }
                is QueueWedgeError.IdentityViolations -> {
                    LocalEventSendState.Failed.VerifiedUserChangedIdentity(queueWedgeError.users.map { UserId(it) })
                }
                is QueueWedgeError.InsecureDevices -> {
                    LocalEventSendState.Failed.VerifiedUserHasUnsignedDevice(
                        devices = queueWedgeError.userDeviceMap.entries.associate { entry ->
                            UserId(entry.key) to entry.value.map { DeviceId(it) }
                        }
                    )
                }
                is QueueWedgeError.GenericApiError -> {
                    if (isRecoverable) {
                        LocalEventSendState.Sending.Event
                    } else {
                        LocalEventSendState.Failed.Unknown(queueWedgeError.msg)
                    }
                }
                is QueueWedgeError.InvalidMimeType -> {
                    LocalEventSendState.Failed.InvalidMimeType(queueWedgeError.mimeType)
                }
                is QueueWedgeError.MissingMediaContent -> {
                    LocalEventSendState.Failed.MissingMediaContent
                }
            }
        }
        is RustEventSendState.Sent -> LocalEventSendState.Sent(EventId(eventId))
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
        RustEventItemOrigin.CACHE -> TimelineItemEventOrigin.CACHE
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
        ShieldStateCode.MISMATCHED_SENDER -> MessageShield.MismatchedSender(isCritical)
    }
}

private fun EventOrTransactionId.eventId(): EventId? {
    return (this as? EventOrTransactionId.EventId)?.let { EventId(it.eventId) }
}

private fun EventOrTransactionId.transactionId(): TransactionId? {
    return (this as? EventOrTransactionId.TransactionId)?.let { TransactionId(it.transactionId) }
}
