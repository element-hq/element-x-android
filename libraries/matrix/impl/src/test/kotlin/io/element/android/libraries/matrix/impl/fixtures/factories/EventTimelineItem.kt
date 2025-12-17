/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.factories

import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiLazyTimelineItemProvider
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import org.matrix.rustcomponents.sdk.EventOrTransactionId
import org.matrix.rustcomponents.sdk.EventSendState
import org.matrix.rustcomponents.sdk.EventTimelineItem
import org.matrix.rustcomponents.sdk.EventTimelineItemDebugInfo
import org.matrix.rustcomponents.sdk.ProfileDetails
import org.matrix.rustcomponents.sdk.Receipt
import org.matrix.rustcomponents.sdk.ShieldState
import org.matrix.rustcomponents.sdk.TimelineItemContent
import uniffi.matrix_sdk_ui.EventItemOrigin

fun aRustEventTimelineItem(
    isRemote: Boolean = true,
    eventOrTransactionId: EventOrTransactionId = EventOrTransactionId.EventId(AN_EVENT_ID.value),
    sender: String = A_USER_ID.value,
    senderProfile: ProfileDetails = ProfileDetails.Unavailable,
    isOwn: Boolean = true,
    isEditable: Boolean = true,
    content: TimelineItemContent = aRustTimelineItemMessageContent(),
    timestamp: ULong = 0uL,
    debugInfo: EventTimelineItemDebugInfo = anEventTimelineItemDebugInfo(),
    localSendState: EventSendState? = null,
    readReceipts: Map<String, Receipt> = emptyMap(),
    origin: EventItemOrigin? = EventItemOrigin.SYNC,
    canBeRepliedTo: Boolean = true,
    shieldsState: ShieldState? = null,
    localCreatedAt: ULong? = null,
) = EventTimelineItem(
    isRemote = isRemote,
    eventOrTransactionId = eventOrTransactionId,
    sender = sender,
    senderProfile = senderProfile,
    timestamp = timestamp,
    isOwn = isOwn,
    isEditable = isEditable,
    canBeRepliedTo = canBeRepliedTo,
    content = content,
    localSendState = localSendState,
    readReceipts = readReceipts,
    origin = origin,
    localCreatedAt = localCreatedAt,
    lazyProvider = FakeFfiLazyTimelineItemProvider(
        debugInfo = debugInfo,
        shieldsState = shieldsState,
    )
)
