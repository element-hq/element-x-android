/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.loggedin

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.sync.SyncState
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.sync.FakeSyncService
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SendQueuesTest {
    private val matrixClient = FakeMatrixClient()
    private val syncService = FakeSyncService(initialSyncState = SyncState.Running)
    private val sut = SendQueues(matrixClient, syncService)

    @Test
    fun `test network status online and sending queue failed`() = runTest {
        val sendQueueDisabledFlow = MutableSharedFlow<RoomId>(replay = 1)
        val setAllSendQueuesEnabledLambda = lambdaRecorder { _: Boolean -> }
        matrixClient.sendQueueDisabledFlow = sendQueueDisabledFlow
        matrixClient.setAllSendQueuesEnabledLambda = setAllSendQueuesEnabledLambda
        val setRoomSendQueueEnabledLambda = lambdaRecorder { _: Boolean -> }
        val room = FakeJoinedRoom(
            setSendQueueEnabledResult = setRoomSendQueueEnabledLambda
        )
        matrixClient.givenGetRoomResult(room.roomId, room)
        sut.launchIn(backgroundScope)

        sendQueueDisabledFlow.emit(room.roomId)
        advanceTimeBy(SEND_QUEUES_RETRY_DELAY_MILLIS)
        runCurrent()

        assert(setAllSendQueuesEnabledLambda)
            .isCalledOnce()
            .with(value(true))

        assert(setRoomSendQueueEnabledLambda).isNeverCalled()
    }

    @Test
    fun `test sync state offline and sending queue failed`() = runTest {
        val sendQueueDisabledFlow = MutableSharedFlow<RoomId>(replay = 1)

        val setAllSendQueuesEnabledLambda = lambdaRecorder { _: Boolean -> }
        matrixClient.sendQueueDisabledFlow = sendQueueDisabledFlow
        matrixClient.setAllSendQueuesEnabledLambda = setAllSendQueuesEnabledLambda
        syncService.emitSyncState(SyncState.Offline)
        val setRoomSendQueueEnabledLambda = lambdaRecorder { _: Boolean -> }
        val room = FakeJoinedRoom(
            setSendQueueEnabledResult = setRoomSendQueueEnabledLambda
        )
        matrixClient.givenGetRoomResult(room.roomId, room)

        sut.launchIn(backgroundScope)

        sendQueueDisabledFlow.emit(room.roomId)
        advanceTimeBy(SEND_QUEUES_RETRY_DELAY_MILLIS)
        runCurrent()

        assert(setAllSendQueuesEnabledLambda).isNeverCalled()
        assert(setRoomSendQueueEnabledLambda).isNeverCalled()
    }

    @Test
    fun `test sync state running enables all the queues without any error`() = runTest {
        val setAllSendQueuesEnabledLambda = lambdaRecorder { _: Boolean -> }
        matrixClient.sendQueueDisabledFlow = MutableSharedFlow()
        matrixClient.setAllSendQueuesEnabledLambda = setAllSendQueuesEnabledLambda

        sut.launchIn(backgroundScope)

        advanceTimeBy(SEND_QUEUES_RETRY_DELAY_MILLIS)
        runCurrent()

        assert(setAllSendQueuesEnabledLambda)
            .isCalledOnce()
            .with(value(true))
    }

    @Test
    fun `test sync state getting back to running enables all the queues without any error`() = runTest {
        val setAllSendQueuesEnabledLambda = lambdaRecorder { _: Boolean -> }
        matrixClient.sendQueueDisabledFlow = MutableSharedFlow()
        matrixClient.setAllSendQueuesEnabledLambda = setAllSendQueuesEnabledLambda
        syncService.emitSyncState(SyncState.Offline)

        sut.launchIn(backgroundScope)

        advanceTimeBy(SEND_QUEUES_RETRY_DELAY_MILLIS)
        runCurrent()
        assert(setAllSendQueuesEnabledLambda).isNeverCalled()

        syncService.emitSyncState(SyncState.Running)
        advanceTimeBy(SEND_QUEUES_RETRY_DELAY_MILLIS)
        runCurrent()

        assert(setAllSendQueuesEnabledLambda)
            .isCalledOnce()
            .with(value(true))
    }

    @Test
    fun `test sending queue failed while offline then sync state getting back to running`() = runTest {
        val sendQueueDisabledFlow = MutableSharedFlow<RoomId>(replay = 1)
        val setAllSendQueuesEnabledLambda = lambdaRecorder { _: Boolean -> }
        matrixClient.sendQueueDisabledFlow = sendQueueDisabledFlow
        matrixClient.setAllSendQueuesEnabledLambda = setAllSendQueuesEnabledLambda
        syncService.emitSyncState(SyncState.Offline)

        sut.launchIn(backgroundScope)

        sendQueueDisabledFlow.emit(A_ROOM_ID)
        advanceTimeBy(SEND_QUEUES_RETRY_DELAY_MILLIS)
        runCurrent()
        assert(setAllSendQueuesEnabledLambda).isNeverCalled()

        syncService.emitSyncState(SyncState.Running)
        advanceTimeBy(SEND_QUEUES_RETRY_DELAY_MILLIS)
        runCurrent()

        assert(setAllSendQueuesEnabledLambda)
            .isCalledOnce()
            .with(value(true))
    }

    @Test
    fun `test sending queue failed after the queues have been enabled enables all the queues again`() = runTest {
        val sendQueueDisabledFlow = MutableSharedFlow<RoomId>()
        val setAllSendQueuesEnabledLambda = lambdaRecorder { _: Boolean -> }
        matrixClient.sendQueueDisabledFlow = sendQueueDisabledFlow
        matrixClient.setAllSendQueuesEnabledLambda = setAllSendQueuesEnabledLambda

        sut.launchIn(backgroundScope)

        advanceTimeBy(SEND_QUEUES_RETRY_DELAY_MILLIS)
        runCurrent()
        assert(setAllSendQueuesEnabledLambda).isCalledOnce()

        sendQueueDisabledFlow.emit(A_ROOM_ID)
        advanceTimeBy(SEND_QUEUES_RETRY_DELAY_MILLIS)
        runCurrent()

        assert(setAllSendQueuesEnabledLambda)
            .isCalledExactly(2)
            .withSequence(listOf(value(true)), listOf(value(true)))
    }

    @Test
    fun `test sync state getting back to running several times enables all the queues each time`() = runTest {
        val setAllSendQueuesEnabledLambda = lambdaRecorder { _: Boolean -> }
        matrixClient.sendQueueDisabledFlow = MutableSharedFlow()
        matrixClient.setAllSendQueuesEnabledLambda = setAllSendQueuesEnabledLambda

        sut.launchIn(backgroundScope)

        advanceTimeBy(SEND_QUEUES_RETRY_DELAY_MILLIS)
        runCurrent()
        syncService.emitSyncState(SyncState.Offline)
        advanceTimeBy(SEND_QUEUES_RETRY_DELAY_MILLIS)
        runCurrent()
        assert(setAllSendQueuesEnabledLambda).isCalledOnce()

        syncService.emitSyncState(SyncState.Running)
        advanceTimeBy(SEND_QUEUES_RETRY_DELAY_MILLIS)
        runCurrent()

        assert(setAllSendQueuesEnabledLambda)
            .isCalledExactly(2)
            .withSequence(listOf(value(true)), listOf(value(true)))
    }
}
