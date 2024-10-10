/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
import io.element.android.tests.testutils.lambda.lambdaRecorder
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
        val lambda = lambdaRecorder<PushData, Unit>(ensureNeverCalled = true) { }
        val vectorFirebaseMessagingService = createVectorFirebaseMessagingService(
            pushHandler = FakePushHandler(handleResult = lambda)
        )
        vectorFirebaseMessagingService.onMessageReceived(RemoteMessage(Bundle()))
    }

    @Test
    fun `test receiving valid data`() = runTest {
        val lambda = lambdaRecorder<PushData, Unit> { }
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
        val lambda = lambdaRecorder<String, Unit> { }
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
