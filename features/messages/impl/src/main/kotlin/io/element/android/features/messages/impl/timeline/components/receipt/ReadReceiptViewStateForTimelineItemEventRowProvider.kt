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
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState

class ReadReceiptViewStateForTimelineItemEventRowProvider :
    PreviewParameterProvider<ReadReceiptViewState> {
    override val values: Sequence<ReadReceiptViewState>
        get() = sequenceOf(
            aReadReceiptViewState(
                sendState = LocalEventSendState.Sending.Event,
            ),
            aReadReceiptViewState(
                sendState = LocalEventSendState.Sent(EventId("\$eventId")),
            ),
            aReadReceiptViewState(
                sendState = LocalEventSendState.Sent(EventId("\$eventId")),
                receipts = mutableListOf<ReadReceiptData>().apply {
                    repeat(5) {
                        add(
                            aReadReceiptData(
                                it
                            )
                        )
                    }
                },
            ),
        )
}
