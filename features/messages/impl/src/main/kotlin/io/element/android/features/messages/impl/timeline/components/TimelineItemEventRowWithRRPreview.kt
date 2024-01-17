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

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.aTimelineItemReactions
import io.element.android.features.messages.impl.timeline.components.receipt.ReadReceiptViewState
import io.element.android.features.messages.impl.timeline.components.receipt.ReadReceiptViewStateForTimelineItemEventRowProvider
import io.element.android.features.messages.impl.timeline.model.TimelineItemReadReceipts
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

// Note: I add to reduce the size of the fun name, or it does not compile.
// Previous name: TimelineItemEventRowWithSendingStateAndReadReceiptPreview
@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowWithRRPreview(
    @PreviewParameter(ReadReceiptViewStateForTimelineItemEventRowProvider::class) state: ReadReceiptViewState,
) = ElementPreview {
    Column {
        // A message from someone else
        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                isMine = false,
                sendState = null,
                content = aTimelineItemTextContent().copy(
                    body = "A message from someone else"
                ),
                timelineItemReactions = aTimelineItemReactions(count = 0),
                readReceiptState = TimelineItemReadReceipts(state.receipts),
            ),
            isLastOutgoingMessage = false,
        )
        // A message from current user
        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                isMine = true,
                sendState = state.sendState,
                content = aTimelineItemTextContent().copy(
                    body = "A message from me"
                ),
                timelineItemReactions = aTimelineItemReactions(count = 0),
                readReceiptState = TimelineItemReadReceipts(state.receipts),
            ),
            isLastOutgoingMessage = false,
        )
        // Another message from current user
        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                isMine = true,
                sendState = state.sendState,
                content = aTimelineItemTextContent().copy(
                    body = "A last message from me"
                ),
                timelineItemReactions = aTimelineItemReactions(count = 0),
                readReceiptState = TimelineItemReadReceipts(state.receipts),
            ),
            isLastOutgoingMessage = true,
        )
    }
}
