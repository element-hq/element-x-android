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

package io.element.android.features.call.utils

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import io.element.android.features.call.impl.services.IncomingCallService
import io.element.android.features.call.impl.utils.ActiveCall
import io.element.android.features.call.impl.utils.CallState
import io.element.android.features.call.impl.utils.DefaultActiveCallManager
import io.element.android.features.call.test.aCallNotificationData
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.push.test.notifications.FakeOnMissedCallNotificationHandler
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows

@RunWith(RobolectricTestRunner::class)
class DefaultActiveCallManagerTest {
    @Test
    fun `registerIncomingCall - sets the incoming call as active`() = runTest {
        val manager = createActiveCallManager()

        assertThat(manager.activeCall.value).isNull()

        val callNotificationData = aCallNotificationData()
        manager.registerIncomingCall(callNotificationData)

        assertThat(manager.activeCall.value).isEqualTo(
            ActiveCall(
                sessionId = callNotificationData.sessionId,
                roomId = callNotificationData.roomId,
                callState = CallState.Ringing(callNotificationData)
            )
        )

        assertServiceStarted()
    }

    @Test
    fun `registerIncomingCall - when there is an already active call does nothing`() = runTest {
        val manager = createActiveCallManager()

        // Register existing call
        val callNotificationData = aCallNotificationData()
        manager.registerIncomingCall(callNotificationData)
        val activeCall = manager.activeCall.value

        // Now add a new call
        manager.registerIncomingCall(aCallNotificationData(roomId = A_ROOM_ID_2))

        assertThat(manager.activeCall.value).isEqualTo(activeCall)
        assertThat(manager.activeCall.value?.roomId).isNotEqualTo(A_ROOM_ID_2)
    }

    @Test
    fun `incomingCallTimedOut - when there isn't an active call does nothing`() = runTest {
        val addMissedCallNotificationLambda = lambdaRecorder<SessionId, RoomId, EventId, Unit> { _, _, _ -> }
        val manager = createActiveCallManager(
            onMissedCallNotificationHandler = FakeOnMissedCallNotificationHandler(addMissedCallNotificationLambda = addMissedCallNotificationLambda)
        )

        manager.incomingCallTimedOut()

        addMissedCallNotificationLambda.assertions().isNeverCalled()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `incomingCallTimedOut - when there is an active call removes it and adds a missed call notification`() = runTest {
        val addMissedCallNotificationLambda = lambdaRecorder<SessionId, RoomId, EventId, Unit> { _, _, _ -> }
        val manager = createActiveCallManager(
            onMissedCallNotificationHandler = FakeOnMissedCallNotificationHandler(addMissedCallNotificationLambda = addMissedCallNotificationLambda)
        )

        manager.registerIncomingCall(aCallNotificationData())
        assertThat(manager.activeCall.value).isNotNull()

        manager.incomingCallTimedOut()
        assertThat(manager.activeCall.value).isNull()

        runCurrent()

        addMissedCallNotificationLambda.assertions().isCalledOnce()

        asserServiceStopped()
    }

    @Test
    fun `hungUpCall - removes existing call`() = runTest {
        val manager = createActiveCallManager()

        manager.registerIncomingCall(aCallNotificationData())
        assertThat(manager.activeCall.value).isNotNull()

        manager.hungUpCall()
        assertThat(manager.activeCall.value).isNull()

        asserServiceStopped()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `joinedCall - register an ongoing call and tries sending the call notify event`() = runTest {
        val sendCallNotifyLambda = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val room = FakeMatrixRoom(sendCallNotificationIfNeededResult = sendCallNotifyLambda)
        val client = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, room)
        }
        val manager = createActiveCallManager(
            matrixClientProvider = FakeMatrixClientProvider(getClient = { Result.success(client) }),
        )
        assertThat(manager.activeCall.value).isNull()

        manager.joinedCall(A_SESSION_ID, A_ROOM_ID)
        assertThat(manager.activeCall.value).isEqualTo(
            ActiveCall(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                callState = CallState.InCall,
            )
        )

        runCurrent()

        sendCallNotifyLambda.assertions().isCalledOnce()

        asserServiceStopped()
    }

    private fun TestScope.createActiveCallManager(
        matrixClientProvider: FakeMatrixClientProvider = FakeMatrixClientProvider(),
        onMissedCallNotificationHandler: FakeOnMissedCallNotificationHandler = FakeOnMissedCallNotificationHandler(),
    ) = DefaultActiveCallManager(
        context = InstrumentationRegistry.getInstrumentation().targetContext,
        coroutineScope = this,
        matrixClientProvider = matrixClientProvider,
        onMissedCallNotificationHandler = onMissedCallNotificationHandler,
    )

    private fun assertServiceStarted() {
        val expectedIntent = Intent(InstrumentationRegistry.getInstrumentation().targetContext, IncomingCallService::class.java)
        val intent = Shadows.shadowOf(RuntimeEnvironment.getApplication()).nextStartedService
        assertThat(intent.component).isEqualTo(expectedIntent.component)
    }

    private fun asserServiceStopped() {
        val expectedIntent = Intent(InstrumentationRegistry.getInstrumentation().targetContext, IncomingCallService::class.java)
        val intent = Shadows.shadowOf(RuntimeEnvironment.getApplication()).nextStoppedService
        assertThat(intent.component).isEqualTo(expectedIntent.component)
    }
}
