/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.SendQueueUpdate
import io.element.android.libraries.matrix.test.A_TRANSACTION_ID
import org.junit.Test
import org.matrix.rustcomponents.sdk.QueueWedgeError
import org.matrix.rustcomponents.sdk.RoomSendQueueUpdate

class SendQueueUpdatesExtTest {
    @Test
    fun `map SendError keeps isRecoverable`() {
        listOf(true, false).forEach { isRecoverable ->
            val update = RoomSendQueueUpdate.SendError(
                transactionId = A_TRANSACTION_ID.value,
                error = QueueWedgeError.GenericApiError("An error"),
                isRecoverable = isRecoverable,
            )
            assertThat(update.map()).isEqualTo(
                SendQueueUpdate.SendError(A_TRANSACTION_ID, isRecoverable = isRecoverable)
            )
        }
    }
}
