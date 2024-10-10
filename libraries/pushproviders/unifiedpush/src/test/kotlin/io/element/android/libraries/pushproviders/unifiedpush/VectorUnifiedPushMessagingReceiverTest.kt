/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.libraries.pushproviders.unifiedpush

import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SECRET
import io.element.android.libraries.push.test.test.FakePushHandler
import io.element.android.libraries.pushproviders.api.PushData
import io.element.android.libraries.pushproviders.api.PushHandler
import io.element.android.libraries.pushproviders.unifiedpush.registration.EndpointRegistrationHandler
import io.element.android.libraries.pushproviders.unifiedpush.registration.RegistrationResult
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
class VectorUnifiedPushMessagingReceiverTest {
    @Test
    fun `onUnregistered does nothing`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val vectorUnifiedPushMessagingReceiver = createVectorUnifiedPushMessagingReceiver()
        vectorUnifiedPushMessagingReceiver.onUnregistered(context, A_SECRET)
    }

    @Test
    fun `onRegistrationFailed does nothing`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val vectorUnifiedPushMessagingReceiver = createVectorUnifiedPushMessagingReceiver()
        vectorUnifiedPushMessagingReceiver.onRegistrationFailed(context, A_SECRET)
    }

    @Test
    fun `onMessage valid invoke the push handler`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val pushHandlerResult = lambdaRecorder<PushData, Unit> {}
        val vectorUnifiedPushMessagingReceiver = createVectorUnifiedPushMessagingReceiver(
            pushHandler = FakePushHandler(
                handleResult = pushHandlerResult
            ),
        )
        vectorUnifiedPushMessagingReceiver.onMessage(context, UnifiedPushParserTest.UNIFIED_PUSH_DATA.toByteArray(), A_SECRET)
        advanceUntilIdle()
        pushHandlerResult.assertions()
            .isCalledOnce()
            .with(
                value(
                    PushData(
                        eventId = AN_EVENT_ID,
                        roomId = A_ROOM_ID,
                        unread = 1,
                        clientSecret = A_SECRET
                    )
                )
            )
    }

    @Test
    fun `onMessage invalid does not invoke the push handler`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val pushHandlerResult = lambdaRecorder<PushData, Unit> {}
        val vectorUnifiedPushMessagingReceiver = createVectorUnifiedPushMessagingReceiver(
            pushHandler = FakePushHandler(
                handleResult = pushHandlerResult
            ),
        )
        vectorUnifiedPushMessagingReceiver.onMessage(context, "".toByteArray(), A_SECRET)
        advanceUntilIdle()
        pushHandlerResult.assertions()
            .isNeverCalled()
    }

    @Test
    fun `onNewEndpoint run the expected tasks`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val storePushGatewayResult = lambdaRecorder<String, String?, Unit> { _, _ -> }
        val storeUpEndpointResult = lambdaRecorder<String, String?, Unit> { _, _ -> }
        val unifiedPushStore = FakeUnifiedPushStore(
            storePushGatewayResult = storePushGatewayResult,
            storeUpEndpointResult = storeUpEndpointResult,
        )
        val endpointRegistrationHandler = EndpointRegistrationHandler()
        val handleResult = lambdaRecorder<String, String, String, Result<Unit>> { _, _, _ -> Result.success(Unit) }
        val unifiedPushNewGatewayHandler = FakeUnifiedPushNewGatewayHandler(
            handleResult = handleResult
        )
        val vectorUnifiedPushMessagingReceiver = createVectorUnifiedPushMessagingReceiver(
            unifiedPushStore = unifiedPushStore,
            unifiedPushGatewayResolver = FakeUnifiedPushGatewayResolver(
                getGatewayResult = { "aGateway" }
            ),
            endpointRegistrationHandler = endpointRegistrationHandler,
            unifiedPushNewGatewayHandler = unifiedPushNewGatewayHandler,
        )
        endpointRegistrationHandler.state.test {
            vectorUnifiedPushMessagingReceiver.onNewEndpoint(context, "anEndpoint", A_SECRET)
            advanceUntilIdle()
            assertThat(awaitItem()).isEqualTo(
                RegistrationResult(
                    clientSecret = A_SECRET,
                    result = Result.success(Unit)
                )
            )
        }
        storePushGatewayResult.assertions()
            .isCalledOnce()
            .with(value(A_SECRET), value("aGateway"))
        storeUpEndpointResult.assertions()
            .isCalledOnce()
            .with(value(A_SECRET), value("anEndpoint"))
    }

    @Test
    fun `onNewEndpoint, if registration fails, the endpoint should not be stored`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val storePushGatewayResult = lambdaRecorder<String, String?, Unit> { _, _ -> }
        val storeUpEndpointResult = lambdaRecorder<String, String?, Unit> { _, _ -> }
        val unifiedPushStore = FakeUnifiedPushStore(
            storePushGatewayResult = storePushGatewayResult,
            storeUpEndpointResult = storeUpEndpointResult,
        )
        val endpointRegistrationHandler = EndpointRegistrationHandler()
        val handleResult = lambdaRecorder<String, String, String, Result<Unit>> { _, _, _ -> Result.failure(AN_EXCEPTION) }
        val unifiedPushNewGatewayHandler = FakeUnifiedPushNewGatewayHandler(
            handleResult = handleResult
        )
        val vectorUnifiedPushMessagingReceiver = createVectorUnifiedPushMessagingReceiver(
            unifiedPushStore = unifiedPushStore,
            unifiedPushGatewayResolver = FakeUnifiedPushGatewayResolver(
                getGatewayResult = { "aGateway" }
            ),
            endpointRegistrationHandler = endpointRegistrationHandler,
            unifiedPushNewGatewayHandler = unifiedPushNewGatewayHandler,
        )
        endpointRegistrationHandler.state.test {
            vectorUnifiedPushMessagingReceiver.onNewEndpoint(context, "anEndpoint", A_SECRET)
            advanceUntilIdle()
            assertThat(awaitItem()).isEqualTo(
                RegistrationResult(
                    clientSecret = A_SECRET,
                    result = Result.failure(AN_EXCEPTION)
                )
            )
        }
        storePushGatewayResult.assertions()
            .isCalledOnce()
            .with(value(A_SECRET), value("aGateway"))
        storeUpEndpointResult.assertions()
            .isNeverCalled()
    }

    private fun TestScope.createVectorUnifiedPushMessagingReceiver(
        pushHandler: PushHandler = FakePushHandler(),
        unifiedPushStore: UnifiedPushStore = FakeUnifiedPushStore(),
        unifiedPushGatewayResolver: UnifiedPushGatewayResolver = FakeUnifiedPushGatewayResolver(),
        unifiedPushNewGatewayHandler: UnifiedPushNewGatewayHandler = FakeUnifiedPushNewGatewayHandler(),
        endpointRegistrationHandler: EndpointRegistrationHandler = EndpointRegistrationHandler(),
    ): VectorUnifiedPushMessagingReceiver {
        return VectorUnifiedPushMessagingReceiver().apply {
            this.pushParser = UnifiedPushParser()
            this.pushHandler = pushHandler
            this.guardServiceStarter = NoopGuardServiceStarter()
            this.unifiedPushStore = unifiedPushStore
            this.unifiedPushGatewayResolver = unifiedPushGatewayResolver
            this.newGatewayHandler = unifiedPushNewGatewayHandler
            this.endpointRegistrationHandler = endpointRegistrationHandler
            this.coroutineScope = this@createVectorUnifiedPushMessagingReceiver
        }
    }
}
