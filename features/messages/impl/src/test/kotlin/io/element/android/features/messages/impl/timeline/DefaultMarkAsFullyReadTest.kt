/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.messages.impl.timeline

import io.element.android.libraries.matrix.api.timeline.ReceiptType
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultMarkAsFullyReadTest {
    @Test
    fun `When room is not found, then no exception is thrown`() = runTest {
        val markAsFullyRead = DefaultMarkAsFullyRead(
            FakeMatrixClient(
                sessionCoroutineScope = backgroundScope,
            ).apply {
                givenGetRoomResult(A_ROOM_ID, null)
            }
        )
        markAsFullyRead.invoke(A_ROOM_ID)
        runCurrent()
    }

    @Test
    fun `When room is found, the expected method is invoked`() = runTest {
        val markAsReadResult = lambdaRecorder<ReceiptType, Result<Unit>> { Result.success(Unit) }
        val baseRoom = FakeBaseRoom(
            markAsReadResult = markAsReadResult
        )
        val markAsFullyRead = DefaultMarkAsFullyRead(
            FakeMatrixClient(
                sessionCoroutineScope = backgroundScope,
            ).apply {
                givenGetRoomResult(A_ROOM_ID, baseRoom)
            }
        )
        markAsFullyRead.invoke(A_ROOM_ID)
        runCurrent()
        markAsReadResult.assertions().isCalledOnce().with(value(ReceiptType.FULLY_READ))
        baseRoom.assertDestroyed()
    }
}
