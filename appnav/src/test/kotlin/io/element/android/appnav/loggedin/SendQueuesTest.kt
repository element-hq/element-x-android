/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.appnav.loggedin

import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.features.networkmonitor.test.FakeNetworkMonitor
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class) class SendQueuesTest {
    private val matrixClient = FakeMatrixClient()
    private val networkMonitor = FakeNetworkMonitor()
    private val sut = SendQueues(matrixClient, networkMonitor)

    @Test
    fun `test network status online and sending queue failed`() = runTest {
        val sendQueueDisabledFlow = MutableSharedFlow<RoomId>(replay = 1)
        val setAllSendQueuesEnabledLambda = lambdaRecorder { _: Boolean -> }
        matrixClient.sendQueueDisabledFlow = sendQueueDisabledFlow
        matrixClient.setAllSendQueuesEnabledLambda = setAllSendQueuesEnabledLambda
        val setRoomSendQueueEnabledLambda = lambdaRecorder { _: Boolean -> }
        val room = FakeMatrixRoom(
            setSendQueueEnabledLambda = setRoomSendQueueEnabledLambda
        )
        matrixClient.givenGetRoomResult(room.roomId, room)
        sut.launchIn(backgroundScope)

        sendQueueDisabledFlow.emit(room.roomId)
        advanceTimeBy(SEND_QUEUES_RETRY_DELAY_MILLIS)
        runCurrent()

        assert(setAllSendQueuesEnabledLambda)
            .isCalledExactly(2)
            .withSequence(
                listOf(value(true)),
                listOf(value(true)),
            )

        assert(setRoomSendQueueEnabledLambda).isNeverCalled()
    }

    @Test
    fun `test network status offline and sending queue failed`() = runTest {
        val sendQueueDisabledFlow = MutableSharedFlow<RoomId>(replay = 1)

        val setAllSendQueuesEnabledLambda = lambdaRecorder { _: Boolean -> }
        matrixClient.sendQueueDisabledFlow = sendQueueDisabledFlow
        matrixClient.setAllSendQueuesEnabledLambda = setAllSendQueuesEnabledLambda
        networkMonitor.connectivity.value = NetworkStatus.Offline
        val setRoomSendQueueEnabledLambda = lambdaRecorder { _: Boolean -> }
        val room = FakeMatrixRoom(
            setSendQueueEnabledLambda = setRoomSendQueueEnabledLambda
        )
        matrixClient.givenGetRoomResult(room.roomId, room)

        sut.launchIn(backgroundScope)

        sendQueueDisabledFlow.emit(room.roomId)
        advanceTimeBy(SEND_QUEUES_RETRY_DELAY_MILLIS)
        runCurrent()

        assert(setAllSendQueuesEnabledLambda)
            .isCalledOnce()
            .with(value(false))

        assert(setRoomSendQueueEnabledLambda)
            .isNeverCalled()
    }

    @Test
    fun `test network status getting offline and online`() = runTest {
        val setEnableSendingQueueLambda = lambdaRecorder { _: Boolean -> }
        matrixClient.setAllSendQueuesEnabledLambda = setEnableSendingQueueLambda

        sut.launchIn(backgroundScope)
        advanceTimeBy(SEND_QUEUES_RETRY_DELAY_MILLIS)
        networkMonitor.connectivity.value = NetworkStatus.Offline
        advanceTimeBy(SEND_QUEUES_RETRY_DELAY_MILLIS)
        networkMonitor.connectivity.value = NetworkStatus.Online
        advanceTimeBy(SEND_QUEUES_RETRY_DELAY_MILLIS)

        assert(setEnableSendingQueueLambda)
            .isCalledExactly(3)
            .withSequence(
                listOf(value(true)),
                listOf(value(false)),
                listOf(value(true)),
            )
    }
}
