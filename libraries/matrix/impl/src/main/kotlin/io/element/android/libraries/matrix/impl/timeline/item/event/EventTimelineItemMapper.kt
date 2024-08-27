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

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import io.element.android.libraries.matrix.api.timeline.item.event.EventReaction
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
import org.matrix.rustcomponents.sdk.Reaction
import org.matrix.rustcomponents.sdk.ShieldState
import uniffi.matrix_sdk_common.ShieldStateCode
import org.matrix.rustcomponents.sdk.EventSendState as RustEventSendState
import org.matrix.rustcomponents.sdk.EventTimelineItem as RustEventTimelineItem
import org.matrix.rustcomponents.sdk.EventTimelineItemDebugInfo as RustEventTimelineItemDebugInfo
import org.matrix.rustcomponents.sdk.ProfileDetails as RustProfileDetails
import org.matrix.rustcomponents.sdk.Receipt as RustReceipt
import uniffi.matrix_sdk_ui.EventItemOrigin as RustEventItemOrigin

class EventTimelineItemMapper(private val contentMapper: TimelineEventContentMapper = TimelineEventContentMapper()) {
    fun map(eventTimelineItem: RustEventTimelineItem): EventTimelineItem = eventTimelineItem.use {
        EventTimelineItem(
            eventId = it.eventId()?.let(::EventId),
            transactionId = it.transactionId()?.let(::TransactionId),
            isEditable = it.isEditable(),
            canBeRepliedTo = it.canBeRepliedTo(),
            isLocal = it.isLocal(),
            isOwn = it.isOwn(),
            isRemote = it.isRemote(),
            localSendState = it.localSendState()?.map(),
            reactions = it.reactions().map(),
            receipts = it.readReceipts().map(),
            sender = UserId(it.sender()),
            senderProfile = it.senderProfile().map(),
            timestamp = it.timestamp().toLong(),
            content = contentMapper.map(it.content()),
            debugInfo = it.debugInfo().map(),
            origin = it.origin()?.map(),
            messageShield = it.getShield(false)?.map(),
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
        RustEventSendState.NotSentYet -> LocalEventSendState.NotSentYet
        is RustEventSendState.SendingFailed -> {
            if (this.isRecoverable) {
                LocalEventSendState.SendingFailed.Recoverable(this.error)
            } else {
                LocalEventSendState.SendingFailed.Unrecoverable(this.error)
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
        ShieldStateCode.PREVIOUSLY_VERIFIED -> MessageShield.PreviouslyVerified(isCritical)
    }
}
