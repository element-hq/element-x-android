/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.historyvisible

import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeHistoryVisibleAcknowledgementRepository(
    private val acknowledgements: MutableMap<RoomId, MutableStateFlow<Boolean>> = mutableMapOf()
) : HistoryVisibleAcknowledgementRepository {
    override fun hasAcknowledged(roomId: RoomId): Flow<Boolean> {
        return acknowledgements.getOrPut(roomId) {
            MutableStateFlow(false)
        }
    }

    override suspend fun setAcknowledged(roomId: RoomId, value: Boolean) {
        val flow = acknowledgements.getOrPut(roomId) {
            MutableStateFlow(value)
        }
        flow.emit(value)
    }

    companion object {
        /**
         * Create the repository with a pre-existing entry.
         */
        fun withRoom(roomId: RoomId, acknowledged: Boolean = false): FakeHistoryVisibleAcknowledgementRepository {
            return FakeHistoryVisibleAcknowledgementRepository(
                mutableMapOf(
                    roomId to MutableStateFlow(acknowledged)
                )
            )
        }
    }
}
