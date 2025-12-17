/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import kotlin.time.Duration.Companion.seconds

@RunWith(RobolectricTestRunner::class)
class DefaultElementCallEntryPointTest {
    @Test
    fun `startCall - starts ElementCallActivity setup with the needed extras`() = runTest {
        val entryPoint = createEntryPoint()
        entryPoint.startCall(CallType.RoomCall(A_SESSION_ID, A_ROOM_ID))

        val expectedIntent = Intent(InstrumentationRegistry.getInstrumentation().targetContext, ElementCallActivity::class.java)
        val intent = shadowOf(RuntimeEnvironment.getApplication()).nextStartedActivity
        assertThat(intent.component).isEqualTo(expectedIntent.component)
        assertThat(intent.extras?.containsKey("EXTRA_CALL_TYPE")).isTrue()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `handleIncomingCall - registers the incoming call using ActiveCallManager`() = runTest {
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
            expirationTimestamp = 0,
            notificationChannelId = "notificationChannelId",
            textContent = "textContent",
        )

        advanceTimeBy(1.seconds)

        registerIncomingCallLambda.assertions().isCalledOnce()
    }

    private fun TestScope.createEntryPoint(
        activeCallManager: FakeActiveCallManager = FakeActiveCallManager(),
    ) = DefaultElementCallEntryPoint(
        context = InstrumentationRegistry.getInstrumentation().targetContext,
        activeCallManager = activeCallManager,
    )
}
