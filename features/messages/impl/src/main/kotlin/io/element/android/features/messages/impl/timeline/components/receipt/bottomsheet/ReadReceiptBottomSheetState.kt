/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.receipt.bottomsheet

import androidx.compose.runtime.Immutable
import io.element.android.features.messages.impl.timeline.model.TimelineItem

@Immutable
data class ReadReceiptBottomSheetState(
    val selectedEvent: TimelineItem.Event?,
    val eventSink: (ReadReceiptBottomSheetEvents) -> Unit,
)
