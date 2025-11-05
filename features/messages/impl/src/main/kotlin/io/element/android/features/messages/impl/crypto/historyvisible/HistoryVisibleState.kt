/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.historyvisible

import io.element.android.libraries.matrix.api.room.history.RoomHistoryVisibility

data class HistoryVisibleState(
    val roomHistoryVisibility: RoomHistoryVisibility,
    val roomIsEncrypted: Boolean,
    val acknowledged: Boolean,
    val eventSink: (HistoryVisibleEvent) -> Unit,
)
