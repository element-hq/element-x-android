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

package io.element.android.features.call

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import io.element.android.features.call.api.CallType
import io.element.android.features.call.impl.DefaultElementCallEntryPoint
import io.element.android.features.call.impl.notifications.CallNotificationData
import io.element.android.features.call.impl.ui.ElementCallActivity
import io.element.android.features.call.utils.FakeActiveCallManager
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.tests.testutils.lambda.lambdaRecorder
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class DefaultElementCallEntryPointTest {
    @Test
    fun `startCall - starts ElementCallActivity setup with the needed extras`() {
        val entryPoint = createEntryPoint()
        entryPoint.startCall(CallType.RoomCall(A_SESSION_ID, A_ROOM_ID))

        val expectedIntent = Intent(InstrumentationRegistry.getInstrumentation().targetContext, ElementCallActivity::class.java)
        val intent = shadowOf(RuntimeEnvironment.getApplication()).nextStartedActivity
        assertThat(intent.component).isEqualTo(expectedIntent.component)
        assertThat(intent.extras?.containsKey("EXTRA_CALL_TYPE")).isTrue()
    }

    @Test
    fun `handleIncomingCall - registers the incoming call using ActiveCallManager`() {
        val registerIncomingCallLambda = lambdaRecorder<CallNotificationData, Unit> {}
        val activeCallManager = FakeActiveCallManager(registerIncomingCallResult = registerIncomingCallLambda)
        val entryPoint = createEntryPoint(activeCallManager = activeCallManager)

        entryPoint.handleIncomingCall(
            callType = CallType.RoomCall(A_SESSION_ID, A_ROOM_ID),
            eventId = AN_EVENT_ID,
            senderId = A_USER_ID_2,
            roomName = "roomName",
            senderName = "senderName",
            avatarUrl = "avatarUrl",
            timestamp = 0,
            notificationChannelId = "notificationChannelId",
        )

        registerIncomingCallLambda.assertions().isCalledOnce()
    }

    private fun createEntryPoint(
        activeCallManager: FakeActiveCallManager = FakeActiveCallManager(),
    ) = DefaultElementCallEntryPoint(
        context = InstrumentationRegistry.getInstrumentation().targetContext,
        activeCallManager = activeCallManager,
    )
}
