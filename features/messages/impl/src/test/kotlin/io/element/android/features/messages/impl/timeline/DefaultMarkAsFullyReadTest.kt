/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.messages.impl.timeline

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultMarkAsFullyReadTest {
    @Test
    fun `When marking as read fails, no exception is thrown`() = runTest {
        val markAsFullyRead = DefaultMarkAsFullyRead(
            matrixClient = FakeMatrixClient(
                markRoomAsFullyReadResult = { _, _ -> Result.failure(IllegalStateException("Room not found")) },
            ).apply {
                givenGetRoomResult(A_ROOM_ID, null)
            },
            coroutineDispatchers = testCoroutineDispatchers(),
        )
        assertThat(markAsFullyRead.invoke(A_ROOM_ID, AN_EVENT_ID).isFailure).isTrue()
        runCurrent()
    }

    @Test
    fun `When marking as read is successful, the expected method is invoked`() = runTest {
        val markAsFullyReadResult = lambdaRecorder<RoomId, EventId, Result<Unit>> { _, _ -> Result.success(Unit) }
        val markAsFullyRead = DefaultMarkAsFullyRead(
            matrixClient = FakeMatrixClient(
                markRoomAsFullyReadResult = markAsFullyReadResult,
            ),
            coroutineDispatchers = testCoroutineDispatchers(),
        )
        assertThat(markAsFullyRead.invoke(A_ROOM_ID, AN_EVENT_ID).isSuccess).isTrue()
        runCurrent()
        markAsFullyReadResult.assertions().isCalledOnce().with(value(A_ROOM_ID), value(AN_EVENT_ID))
    }
}
