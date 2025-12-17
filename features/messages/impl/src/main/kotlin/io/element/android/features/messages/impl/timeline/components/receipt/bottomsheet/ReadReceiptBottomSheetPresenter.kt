/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.receipt.bottomsheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.architecture.Presenter

@Inject
class ReadReceiptBottomSheetPresenter : Presenter<ReadReceiptBottomSheetState> {
    @Composable
    override fun present(): ReadReceiptBottomSheetState {
        var selectedEvent: TimelineItem.Event? by remember { mutableStateOf(null) }

        fun handleEvent(event: ReadReceiptBottomSheetEvents) {
            @Suppress("LiftReturnOrAssignment")
            when (event) {
                is ReadReceiptBottomSheetEvents.EventSelected -> {
                    selectedEvent = event.event
                }
                ReadReceiptBottomSheetEvents.Dismiss -> {
                    selectedEvent = null
                }
            }
        }

        return ReadReceiptBottomSheetState(
            selectedEvent = selectedEvent,
            eventSink = ::handleEvent,
        )
    }
}
