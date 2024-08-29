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
