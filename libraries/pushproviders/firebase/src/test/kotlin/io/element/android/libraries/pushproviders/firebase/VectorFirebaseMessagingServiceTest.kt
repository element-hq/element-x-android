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

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.libraries.pushproviders.firebase

import android.os.Bundle
import com.google.firebase.messaging.RemoteMessage
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SECRET
import io.element.android.libraries.push.test.test.FakePushHandler
import io.element.android.libraries.pushproviders.api.PushData
import io.element.android.libraries.pushproviders.api.PushHandler
import io.element.android.tests.testutils.lambda.lambdaSuspendRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VectorFirebaseMessagingServiceTest {
    @Test
    fun `test receiving invalid data`() = runTest {
        val lambda = lambdaSuspendRecorder<PushData, Unit>(ensureNeverCalled = true) { }
        val vectorFirebaseMessagingService = createVectorFirebaseMessagingService(
            pushHandler = FakePushHandler(handleResult = lambda)
        )
        vectorFirebaseMessagingService.onMessageReceived(RemoteMessage(Bundle()))
    }

    @Test
    fun `test receiving valid data`() = runTest {
        val lambda = lambdaSuspendRecorder<PushData, Unit> { }
        val vectorFirebaseMessagingService = createVectorFirebaseMessagingService(
            pushHandler = FakePushHandler(handleResult = lambda)
        )
        vectorFirebaseMessagingService.onMessageReceived(
            message = RemoteMessage(
                Bundle().apply {
                    putString("event_id", AN_EVENT_ID.value)
                    putString("room_id", A_ROOM_ID.value)
                    putString("cs", A_SECRET)
                },
            )
        )
        advanceUntilIdle()
        lambda.assertions()
            .isCalledOnce()
            .with(value(PushData(AN_EVENT_ID, A_ROOM_ID, null, A_SECRET)))
    }

    @Test
    fun `test new token is forwarded to the handler`() = runTest {
        val lambda = lambdaSuspendRecorder<String, Unit> { }
        val vectorFirebaseMessagingService = createVectorFirebaseMessagingService(
            firebaseNewTokenHandler = FakeFirebaseNewTokenHandler(handleResult = lambda)
        )
        vectorFirebaseMessagingService.onNewToken("aToken")
        advanceUntilIdle()
        lambda.assertions()
            .isCalledOnce()
            .with(value("aToken"))
    }

    private fun TestScope.createVectorFirebaseMessagingService(
        firebaseNewTokenHandler: FirebaseNewTokenHandler = FakeFirebaseNewTokenHandler(),
        pushHandler: PushHandler = FakePushHandler(),
    ): VectorFirebaseMessagingService {
        return VectorFirebaseMessagingService().apply {
            this.firebaseNewTokenHandler = firebaseNewTokenHandler
            this.pushParser = FirebasePushParser()
            this.pushHandler = pushHandler
            this.coroutineScope = this@createVectorFirebaseMessagingService
        }
    }
}
