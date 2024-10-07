/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.receipt.bottomsheet

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.components.receipt.ReadReceiptViewStateProvider
import io.element.android.features.messages.impl.timeline.model.TimelineItemReadReceipts
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import kotlinx.collections.immutable.toImmutableList

class ReadReceiptBottomSheetStateProvider : PreviewParameterProvider<ReadReceiptBottomSheetState> {
    // Reuse the provider ReadReceiptViewStateProvider
    private val readReceiptViewStateProvider = ReadReceiptViewStateProvider()
    override val values: Sequence<ReadReceiptBottomSheetState> = readReceiptViewStateProvider.values
        .filter { it.sendState is LocalEventSendState.Sent }
        .map { readReceiptViewState ->
            ReadReceiptBottomSheetState(
                isDebugBuild = false,
                selectedEvent = aTimelineItemEvent(
                    readReceiptState = TimelineItemReadReceipts(
                        receipts = readReceiptViewState.receipts.map { readReceiptData ->
                            readReceiptData
                                .copy(avatarData = readReceiptData.avatarData.copy(id = "@${readReceiptData.avatarData.id}:localhost"))
                        }.toImmutableList()
                    )
                ),
                eventSink = {},
            )
        }
}
