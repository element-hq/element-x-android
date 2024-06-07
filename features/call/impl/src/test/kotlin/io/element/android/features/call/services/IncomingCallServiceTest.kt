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

package io.element.android.features.call.services

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import io.element.android.features.call.impl.notifications.CallNotificationData
import io.element.android.features.call.impl.notifications.RingingCallNotificationCreator
import io.element.android.features.call.impl.services.IncomingCallService
import io.element.android.features.call.test.aCallNotificationData
import io.element.android.features.call.utils.FakeActiveCallManager
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.push.test.notifications.FakeImageLoaderHolder
import io.element.android.libraries.push.test.notifications.push.FakeNotificationBitmapLoader
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class IncomingCallServiceTest {
    @Test
    fun `onStartCommand - with no intent just returns`() = runTest {
        val registerIncomingCallLambda = lambdaRecorder<CallNotificationData, Unit> {}
        val service = createService(
            fakeActiveCallManager = FakeActiveCallManager(registerIncomingCallResult = registerIncomingCallLambda)
        )

        service.onStartCommand(null, 0, 0)

        registerIncomingCallLambda.assertions().isNeverCalled()
    }

    @Test
    fun `onStartCommand - with no extras just returns`() = runTest {
        val registerIncomingCallLambda = lambdaRecorder<CallNotificationData, Unit> {}
        val service = createService(
            fakeActiveCallManager = FakeActiveCallManager(registerIncomingCallResult = registerIncomingCallLambda)
        )

        service.onStartCommand(Intent(), 0, 0)

        registerIncomingCallLambda.assertions().isNeverCalled()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `onStartCommand - with extras registers an incoming call`() = runTest {
        val registerIncomingCallLambda = lambdaRecorder<CallNotificationData, Unit> {}
        val incomingCallTimedOutLambda = lambdaRecorder<Unit> {}
        val service = createService(
            fakeActiveCallManager = FakeActiveCallManager(
                registerIncomingCallResult = registerIncomingCallLambda,
                incomingCallTimedOutResult = incomingCallTimedOutLambda,
            )
        )

        service.onStartCommand(
            Intent().putExtra("NOTIFICATION_DATA", aCallNotificationData()),
            0,
            0
        )

        advanceUntilIdle()

        registerIncomingCallLambda.assertions().isCalledOnce()
        // Assume the call wasn't answered in time
        incomingCallTimedOutLambda.assertions().isCalledOnce()
    }

    private fun TestScope.createService(
        fakeActiveCallManager: FakeActiveCallManager = FakeActiveCallManager(),
    ) = IncomingCallService().apply {
        coroutineScope = this@createService
        ringingCallNotificationCreator = RingingCallNotificationCreator(
            context = InstrumentationRegistry.getInstrumentation().targetContext,
            matrixClientProvider = FakeMatrixClientProvider(),
            imageLoaderHolder = FakeImageLoaderHolder(),
            notificationBitmapLoader = FakeNotificationBitmapLoader(),
        )
        activeCallManager = fakeActiveCallManager
    }
}
