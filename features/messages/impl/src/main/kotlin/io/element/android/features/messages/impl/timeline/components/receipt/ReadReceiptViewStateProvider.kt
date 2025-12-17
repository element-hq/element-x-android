/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.receipt

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.messages.impl.timeline.model.ReadReceiptData
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.anAvatarData
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import kotlinx.collections.immutable.toImmutableList

class ReadReceiptViewStateProvider : PreviewParameterProvider<ReadReceiptViewState> {
    override val values: Sequence<ReadReceiptViewState>
        get() = sequenceOf(
            aReadReceiptViewState(),
            aReadReceiptViewState(sendState = LocalEventSendState.Sending.Event),
            aReadReceiptViewState(sendState = LocalEventSendState.Sent(EventId("\$eventId"))),
            aReadReceiptViewState(
                sendState = LocalEventSendState.Sent(EventId("\$eventId")),
                receipts = List(1) { aReadReceiptData(it) },
            ),
            aReadReceiptViewState(
                sendState = LocalEventSendState.Sent(EventId("\$eventId")),
                receipts = List(2) { aReadReceiptData(it) },
            ),
            aReadReceiptViewState(
                sendState = LocalEventSendState.Sent(EventId("\$eventId")),
                receipts = List(3) { aReadReceiptData(it) },
            ),
            aReadReceiptViewState(
                sendState = LocalEventSendState.Sent(EventId("\$eventId")),
                receipts = List(4) { aReadReceiptData(it) },
            ),
            aReadReceiptViewState(
                sendState = LocalEventSendState.Sent(EventId("\$eventId")),
                receipts = List(5) { aReadReceiptData(it) },
            ),
        )
}

internal fun aReadReceiptViewState(
    sendState: LocalEventSendState? = null,
    isLastOutgoingMessage: Boolean = true,
    receipts: List<ReadReceiptData> = emptyList(),
) = ReadReceiptViewState(
    sendState = sendState,
    isLastOutgoingMessage = isLastOutgoingMessage,
    receipts = receipts.toImmutableList(),
)

internal fun aReadReceiptData(
    index: Int,
    avatarData: AvatarData = anAvatarData(
        id = "$index",
        size = AvatarSize.TimelineReadReceipt
    ),
    formattedDate: String = "12:34",
) = ReadReceiptData(
    avatarData = avatarData,
    formattedDate = formattedDate,
)
