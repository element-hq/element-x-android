/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.historyvisible

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.room.history.RoomHistoryVisibility

class HistoryVisibleStateProvider : PreviewParameterProvider<HistoryVisibleState> {
    override val values: Sequence<HistoryVisibleState>
        get() = sequenceOf(
            aHistoryVisibleState(RoomHistoryVisibility.Joined, roomIsEncrypted = false, acknowledged = false),
            aHistoryVisibleState(RoomHistoryVisibility.Shared, roomIsEncrypted = true, acknowledged = false),
            aHistoryVisibleState(RoomHistoryVisibility.Shared, roomIsEncrypted = true, acknowledged = true)
        )
}

internal fun aHistoryVisibleState(
    roomHistoryVisibility: RoomHistoryVisibility = RoomHistoryVisibility.Joined,
    roomIsEncrypted: Boolean = false,
    acknowledged: Boolean = false,
    eventSink: (HistoryVisibleEvent) -> Unit = {},
) = HistoryVisibleState(
    roomHistoryVisibility = roomHistoryVisibility,
    roomIsEncrypted = roomIsEncrypted,
    acknowledged = acknowledged,
    eventSink = eventSink,
)
