/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.libraries.pushproviders.unifiedpush

import android.content.Intent
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
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.unifiedpush.android.connector.FailedReason
import org.unifiedpush.android.connector.data.PublicKeySet
import org.unifiedpush.android.connector.data.PushEndpoint
import org.unifiedpush.android.connector.data.PushMessage

@RunWith(RobolectricTestRunner::class)
class VectorUnifiedPushMessagingReceiverTest {
    @Test
    fun `onReceive does the binding`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val vectorUnifiedPushMessagingReceiver = createVectorUnifiedPushMessagingReceiver()
        // The binding is not found in the test env.
        assertThrows(IllegalStateException::class.java) {
            vectorUnifiedPushMessagingReceiver.onReceive(context, Intent())
        }
    }

    @Test
    fun `onUnregistered invokes the removedGatewayHandler`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val handleResult = lambdaRecorder<String, Result<Unit>> {
            Result.success(Unit)
        }
        val vectorUnifiedPushMessagingReceiver = createVectorUnifiedPushMessagingReceiver(
            removedGatewayHandler = UnifiedPushRemovedGatewayHandler { handleResult(it) },
        )
        vectorUnifiedPushMessagingReceiver.onUnregistered(context, A_SECRET)
        advanceUntilIdle()
        handleResult.assertions().isCalledOnce().with(value(A_SECRET))
    }

    @Test
    fun `onRegistrationFailed does nothing`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val vectorUnifiedPushMessagingReceiver = createVectorUnifiedPushMessagingReceiver()
        vectorUnifiedPushMessagingReceiver.onRegistrationFailed(context, FailedReason.NETWORK, A_SECRET)
    }

    @Test
    fun `onMessage valid invokes the push handler`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val pushHandlerResult = lambdaRecorder<PushData, String, Unit> { _, _ -> }
        val vectorUnifiedPushMessagingReceiver = createVectorUnifiedPushMessagingReceiver(
            pushHandler = FakePushHandler(
                handleResult = pushHandlerResult
            ),
        )
        vectorUnifiedPushMessagingReceiver.onMessage(context, aPushMessage(), A_SECRET)
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
                ),
                value(
                    UnifiedPushConfig.NAME + " - " + A_SECRET
                )
            )
    }

    @Test
    fun `onMessage invalid invokes the push handler invalid method`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val handleInvalidResult = lambdaRecorder<String, String, Unit> { _, _ -> }
        val vectorUnifiedPushMessagingReceiver = createVectorUnifiedPushMessagingReceiver(
            pushHandler = FakePushHandler(
                handleInvalidResult = handleInvalidResult,
            ),
        )
        vectorUnifiedPushMessagingReceiver.onMessage(context, aPushMessage(""), A_SECRET)
        advanceUntilIdle()
        handleInvalidResult.assertions().isCalledOnce()
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
                getGatewayResult = { UnifiedPushGatewayResolverResult.Success("aGateway") }
            ),
            unifiedPushGatewayUrlResolver = FakeUnifiedPushGatewayUrlResolver(
                resolveResult = { _, _ -> "aGatewayUrl" }
            ),
            endpointRegistrationHandler = endpointRegistrationHandler,
            unifiedPushNewGatewayHandler = unifiedPushNewGatewayHandler,
        )
        endpointRegistrationHandler.state.test {
            vectorUnifiedPushMessagingReceiver.onNewEndpoint(context, aPushEndpoint("anEndpoint"), A_SECRET)
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
            .with(value(A_SECRET), value("aGatewayUrl"))
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
                getGatewayResult = { UnifiedPushGatewayResolverResult.Success("aGateway") }
            ),
            unifiedPushGatewayUrlResolver = FakeUnifiedPushGatewayUrlResolver(
                resolveResult = { _, _ -> "aGatewayUrl" }
            ),
            endpointRegistrationHandler = endpointRegistrationHandler,
            unifiedPushNewGatewayHandler = unifiedPushNewGatewayHandler,
        )
        endpointRegistrationHandler.state.test {
            vectorUnifiedPushMessagingReceiver.onNewEndpoint(context, aPushEndpoint(), A_SECRET)
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
            .with(value(A_SECRET), value("aGatewayUrl"))
        storeUpEndpointResult.assertions()
            .isNeverCalled()
    }

    private fun TestScope.createVectorUnifiedPushMessagingReceiver(
        unifiedPushParser: UnifiedPushParser = createUnifiedPushParser(),
        pushHandler: PushHandler = FakePushHandler(),
        unifiedPushStore: UnifiedPushStore = FakeUnifiedPushStore(),
        unifiedPushGatewayResolver: UnifiedPushGatewayResolver = FakeUnifiedPushGatewayResolver(),
        unifiedPushGatewayUrlResolver: UnifiedPushGatewayUrlResolver = FakeUnifiedPushGatewayUrlResolver(),
        unifiedPushNewGatewayHandler: UnifiedPushNewGatewayHandler = FakeUnifiedPushNewGatewayHandler(),
        endpointRegistrationHandler: EndpointRegistrationHandler = EndpointRegistrationHandler(),
        removedGatewayHandler: UnifiedPushRemovedGatewayHandler = UnifiedPushRemovedGatewayHandler { lambdaError() },
    ): VectorUnifiedPushMessagingReceiver {
        return VectorUnifiedPushMessagingReceiver().apply {
            this.pushParser = unifiedPushParser
            this.pushHandler = pushHandler
            this.guardServiceStarter = NoopGuardServiceStarter()
            this.unifiedPushStore = unifiedPushStore
            this.unifiedPushGatewayResolver = unifiedPushGatewayResolver
            this.unifiedPushGatewayUrlResolver = unifiedPushGatewayUrlResolver
            this.newGatewayHandler = unifiedPushNewGatewayHandler
            this.removedGatewayHandler = removedGatewayHandler
            this.endpointRegistrationHandler = endpointRegistrationHandler
            this.coroutineScope = this@createVectorUnifiedPushMessagingReceiver
        }
    }
}

private fun aPushMessage(
    data: String = UnifiedPushParserTest.UNIFIED_PUSH_DATA,
    decrypted: Boolean = true,
) = PushMessage(
    content = data.toByteArray(),
    decrypted = decrypted,
)

private fun aPushEndpoint(
    url: String = "anEndpoint",
    pubKeySet: PublicKeySet? = null,
    temporary: Boolean = false,
) = PushEndpoint(
    url = url,
    pubKeySet = pubKeySet,
    temporary = temporary,
)
