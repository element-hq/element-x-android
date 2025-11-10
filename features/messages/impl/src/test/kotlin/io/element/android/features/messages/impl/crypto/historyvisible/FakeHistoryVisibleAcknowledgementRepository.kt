/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.historyvisible

import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeHistoryVisibleAcknowledgementRepository : HistoryVisibleAcknowledgementRepository {
    private val acknowledgements = mutableMapOf<RoomId, MutableSharedFlow<Boolean>>()

    override fun hasAcknowledged(roomId: RoomId): Flow<Boolean> {
        return acknowledgements.getOrPut(roomId) { MutableSharedFlow() }
    }

    override suspend fun setAcknowledged(roomId: RoomId, value: Boolean) {
        val flow = acknowledgements.getOrPut(roomId) { MutableSharedFlow() }
        flow.emit(value)
    }
}
