/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.factories

import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import org.matrix.rustcomponents.sdk.EventOrTransactionId
import org.matrix.rustcomponents.sdk.EventSendState
import org.matrix.rustcomponents.sdk.EventShieldsProvider
import org.matrix.rustcomponents.sdk.EventTimelineItem
import org.matrix.rustcomponents.sdk.EventTimelineItemDebugInfo
import org.matrix.rustcomponents.sdk.EventTimelineItemDebugInfoProvider
import org.matrix.rustcomponents.sdk.NoPointer
import org.matrix.rustcomponents.sdk.ProfileDetails
import org.matrix.rustcomponents.sdk.Reaction
import org.matrix.rustcomponents.sdk.Receipt
import org.matrix.rustcomponents.sdk.ShieldState
import org.matrix.rustcomponents.sdk.TimelineItemContent
import uniffi.matrix_sdk_ui.EventItemOrigin

fun aRustEventTimelineItem(
    isLocal: Boolean = false,
    isRemote: Boolean = true,
    eventOrTransactionId: EventOrTransactionId = EventOrTransactionId.EventId(AN_EVENT_ID.value),
    sender: String = A_USER_ID.value,
    senderProfile: ProfileDetails = ProfileDetails.Unavailable,
    isOwn: Boolean = true,
    isEditable: Boolean = true,
    content: TimelineItemContent = aRustTimelineItemMessageContent(),
    timestamp: ULong = 0uL,
    reactions: List<Reaction> = emptyList<Reaction>(),
    debugInfo: EventTimelineItemDebugInfo = EventTimelineItemDebugInfo(model = "", originalJson = null, latestEditJson = null),
    localSendState: EventSendState? = null,
    readReceipts: Map<String, Receipt> = emptyMap<String, Receipt>(),
    origin: EventItemOrigin? = EventItemOrigin.SYNC,
    canBeRepliedTo: Boolean = true,
    shieldsState: ShieldState? = null,
) = EventTimelineItem(
    isLocal = isLocal,
    isRemote = isRemote,
    eventOrTransactionId = eventOrTransactionId,
    sender = sender,
    senderProfile = senderProfile,
    timestamp = timestamp,
    isOwn = isOwn,
    isEditable = isEditable,
    canBeRepliedTo = canBeRepliedTo,
    content = content,
    debugInfoProvider = object : EventTimelineItemDebugInfoProvider(NoPointer) {
        override fun get(): EventTimelineItemDebugInfo {
            return debugInfo
        }
    },
    shieldsProvider = object : EventShieldsProvider(NoPointer) {
        override fun getShields(strict: Boolean): ShieldState? {
            return shieldsState
        }
    },
    localSendState = localSendState,
    reactions = reactions,
    readReceipts = readReceipts,
    origin = origin,
)
