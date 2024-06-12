/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    private val room = FakeMatrixRoom()
    private val networkMonitor = FakeNetworkMonitor()
    private val sut = SendQueues(matrixClient, networkMonitor)

    @Test
    fun `test network status online and sending queue failed`() = runTest {
        val sendQueueDisabledFlow = MutableSharedFlow<RoomId>(replay = 1)
        val setAllSendQueuesEnabledLambda = lambdaRecorder { _: Boolean -> }
        matrixClient.sendQueueDisabledFlow = sendQueueDisabledFlow
        matrixClient.setAllSendQueuesEnabledLambda = setAllSendQueuesEnabledLambda
        matrixClient.givenGetRoomResult(room.roomId, room)

        val setRoomSendQueueEnabledLambda = lambdaRecorder { _: Boolean -> }
        room.setSendQueueEnabledLambda = setRoomSendQueueEnabledLambda

        sut.launchIn(backgroundScope)

        sendQueueDisabledFlow.emit(room.roomId)
        advanceTimeBy(SENDING_QUEUE_RETRY_DELAY)
        runCurrent()

        assert(setAllSendQueuesEnabledLambda)
            .isCalledOnce()
            .with(value(true))

        assert(setRoomSendQueueEnabledLambda)
            .isCalledOnce()
            .with(value(true))
    }

    @Test
    fun `test network status offline and sending queue failed`() = runTest {
        val sendQueueDisabledFlow = MutableSharedFlow<RoomId>(replay = 1)

        val setAllSendQueuesEnabledLambda = lambdaRecorder { _: Boolean -> }
        matrixClient.sendQueueDisabledFlow = sendQueueDisabledFlow
        matrixClient.setAllSendQueuesEnabledLambda = setAllSendQueuesEnabledLambda
        networkMonitor.connectivity.value = NetworkStatus.Offline
        matrixClient.givenGetRoomResult(room.roomId, room)

        val setRoomSendQueueEnabledLambda = lambdaRecorder { _: Boolean -> }
        room.setSendQueueEnabledLambda = setRoomSendQueueEnabledLambda

        sut.launchIn(backgroundScope)

        sendQueueDisabledFlow.emit(room.roomId)
        advanceTimeBy(SENDING_QUEUE_RETRY_DELAY)
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
        advanceTimeBy(SENDING_QUEUE_RETRY_DELAY)
        networkMonitor.connectivity.value = NetworkStatus.Offline
        advanceTimeBy(SENDING_QUEUE_RETRY_DELAY)
        networkMonitor.connectivity.value = NetworkStatus.Online
        advanceTimeBy(SENDING_QUEUE_RETRY_DELAY)

        assert(setEnableSendingQueueLambda)
            .isCalledExactly(3)
            .withSequence(
                listOf(value(true)),
                listOf(value(false)),
                listOf(value(true)),
            )
    }
}
