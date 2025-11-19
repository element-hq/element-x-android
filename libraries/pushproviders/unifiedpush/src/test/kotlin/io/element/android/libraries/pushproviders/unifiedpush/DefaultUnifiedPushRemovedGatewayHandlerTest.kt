/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_SECRET
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.push.api.PushService
import io.element.android.libraries.push.test.FakePushService
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.libraries.pushproviders.test.FakePushProvider
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import io.element.android.libraries.pushstore.test.userpushstore.clientsecret.FakePushClientSecret
import io.element.android.tests.testutils.lambda.any
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class DefaultUnifiedPushRemovedGatewayHandlerTest {
    @Test
    fun `handle returns error if the secret is unknown`() = runTest {
        val sut = createDefaultUnifiedPushRemovedGatewayHandler(
            pushClientSecret = FakePushClientSecret(
                getUserIdFromSecretResult = { null },
            ),
        )
        val result = sut.handle(A_SECRET)
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `handle returns error if cannot restore the client`() = runTest {
        val sut = createDefaultUnifiedPushRemovedGatewayHandler(
            pushClientSecret = FakePushClientSecret(
                getUserIdFromSecretResult = { A_SESSION_ID },
            ),
            matrixClientProvider = FakeMatrixClientProvider(
                getClient = { Result.failure(AN_EXCEPTION) },
            ),
        )
        val result = sut.handle(A_SECRET)
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `handle returns error if cannot unregister the pusher, and user is notified`() = runTest {
        val onServiceUnregisteredResult = lambdaRecorder<UserId, Unit> { }
        val sut = createDefaultUnifiedPushRemovedGatewayHandler(
            pushClientSecret = FakePushClientSecret(
                getUserIdFromSecretResult = { A_SESSION_ID },
            ),
            matrixClientProvider = FakeMatrixClientProvider(
                getClient = { Result.success(FakeMatrixClient()) },
            ),
            unregisterUnifiedPushUseCase = FakeUnregisterUnifiedPushUseCase(
                unregisterLambda = { _, _, _ -> Result.failure(AN_EXCEPTION) },
            ),
            pushService = FakePushService(
                onServiceUnregisteredResult = onServiceUnregisteredResult,
            ),
        )
        val result = sut.handle(A_SECRET)
        assertThat(result.isFailure).isTrue()
        onServiceUnregisteredResult.assertions().isCalledOnce().with(value(A_SESSION_ID))
    }

    @Test
    fun `handle returns error if cannot get current push provider, and user is notified`() = runTest {
        val onServiceUnregisteredResult = lambdaRecorder<UserId, Unit> { }
        val sut = createDefaultUnifiedPushRemovedGatewayHandler(
            pushClientSecret = FakePushClientSecret(
                getUserIdFromSecretResult = { A_SESSION_ID },
            ),
            matrixClientProvider = FakeMatrixClientProvider(
                getClient = { Result.success(FakeMatrixClient()) },
            ),
            unregisterUnifiedPushUseCase = FakeUnregisterUnifiedPushUseCase(
                unregisterLambda = { _, _, _ -> Result.success(Unit) },
            ),
            pushService = FakePushService(
                currentPushProvider = { null },
                onServiceUnregisteredResult = onServiceUnregisteredResult,
            ),
        )
        val result = sut.handle(A_SECRET)
        assertThat(result.isFailure).isTrue()
        onServiceUnregisteredResult.assertions().isCalledOnce().with(value(A_SESSION_ID))
    }

    @Test
    fun `handle returns error if cannot get current distributor, and user is notified`() = runTest {
        val onServiceUnregisteredResult = lambdaRecorder<UserId, Unit> { }
        val sut = createDefaultUnifiedPushRemovedGatewayHandler(
            pushClientSecret = FakePushClientSecret(
                getUserIdFromSecretResult = { A_SESSION_ID },
            ),
            matrixClientProvider = FakeMatrixClientProvider(
                getClient = { Result.success(FakeMatrixClient()) },
            ),
            unregisterUnifiedPushUseCase = FakeUnregisterUnifiedPushUseCase(
                unregisterLambda = { _, _, _ -> Result.success(Unit) },
            ),
            pushService = FakePushService(
                currentPushProvider = {
                    FakePushProvider(
                        currentDistributor = { null },
                    )
                },
                onServiceUnregisteredResult = onServiceUnregisteredResult,
            ),
        )
        val result = sut.handle(A_SECRET)
        assertThat(result.isFailure).isTrue()
        onServiceUnregisteredResult.assertions().isCalledOnce().with(value(A_SESSION_ID))
    }

    @Test
    fun `handle returns error if cannot register again, and user is notified`() = runTest {
        val onServiceUnregisteredResult = lambdaRecorder<UserId, Unit> { }
        val sut = createDefaultUnifiedPushRemovedGatewayHandler(
            pushClientSecret = FakePushClientSecret(
                getUserIdFromSecretResult = { A_SESSION_ID },
            ),
            matrixClientProvider = FakeMatrixClientProvider(
                getClient = { Result.success(FakeMatrixClient()) },
            ),
            unregisterUnifiedPushUseCase = FakeUnregisterUnifiedPushUseCase(
                unregisterLambda = { _, _, _ -> Result.success(Unit) },
            ),
            pushService = FakePushService(
                currentPushProvider = {
                    FakePushProvider(
                        currentDistributor = { Distributor("aValue", "aName") },
                    )
                },
                registerWithLambda = { _, _, _ -> Result.failure(AN_EXCEPTION) },
                onServiceUnregisteredResult = onServiceUnregisteredResult,
            ),
        )
        val result = sut.handle(A_SECRET)
        assertThat(result.isFailure).isTrue()
        onServiceUnregisteredResult.assertions().isCalledOnce().with(value(A_SESSION_ID))
    }

    @Test
    fun `handle returns success if can register again, and user is not notified`() = runTest {
        val onServiceUnregisteredResult = lambdaRecorder<UserId, Unit> { }
        val unregisterLambda = lambdaRecorder<MatrixClient, String, Boolean, Result<Unit>> { _, _, _ -> Result.success(Unit) }
        val sut = createDefaultUnifiedPushRemovedGatewayHandler(
            pushClientSecret = FakePushClientSecret(
                getUserIdFromSecretResult = { A_SESSION_ID },
            ),
            matrixClientProvider = FakeMatrixClientProvider(
                getClient = { Result.success(FakeMatrixClient()) },
            ),
            unregisterUnifiedPushUseCase = FakeUnregisterUnifiedPushUseCase(
                unregisterLambda = unregisterLambda,
            ),
            pushService = FakePushService(
                currentPushProvider = {
                    FakePushProvider(
                        currentDistributor = { Distributor("aValue", "aName") },
                    )
                },
                registerWithLambda = { _, _, _ -> Result.success(Unit) },
                onServiceUnregisteredResult = onServiceUnregisteredResult,
            ),
        )
        val result = sut.handle(A_SECRET)
        assertThat(result.isSuccess).isTrue()
        unregisterLambda.assertions().isCalledOnce().with(
            any(),
            value(A_SECRET),
            value(false),
        )
        onServiceUnregisteredResult.assertions().isNeverCalled()
    }

    @Test
    fun `handle returns success if can register again, but after 2 removals user is notified`() = runTest {
        val onServiceUnregisteredResult = lambdaRecorder<UserId, Unit> { }
        val unregisterLambda = lambdaRecorder<MatrixClient, String, Boolean, Result<Unit>> { _, _, _ -> Result.success(Unit) }
        val registerWithLambda = lambdaRecorder<MatrixClient, PushProvider, Distributor, Result<Unit>> { _, _, _ -> Result.success(Unit) }
        val sut = createDefaultUnifiedPushRemovedGatewayHandler(
            pushClientSecret = FakePushClientSecret(
                getUserIdFromSecretResult = { A_SESSION_ID },
            ),
            matrixClientProvider = FakeMatrixClientProvider(
                getClient = { Result.success(FakeMatrixClient()) },
            ),
            unregisterUnifiedPushUseCase = FakeUnregisterUnifiedPushUseCase(
                unregisterLambda = unregisterLambda,
            ),
            pushService = FakePushService(
                currentPushProvider = {
                    FakePushProvider(
                        currentDistributor = { Distributor("aValue", "aName") },
                    )
                },
                registerWithLambda = registerWithLambda,
                onServiceUnregisteredResult = onServiceUnregisteredResult,
            ),
        )
        val result = sut.handle(A_SECRET)
        assertThat(result.isSuccess).isTrue()
        unregisterLambda.assertions().isCalledOnce().with(
            any(),
            value(A_SECRET),
            value(false),
        )
        registerWithLambda.assertions().isCalledOnce()
        onServiceUnregisteredResult.assertions().isNeverCalled()
        // Second attempt in less than 1 minute
        val result2 = sut.handle(A_SECRET)
        assertThat(result2.isFailure).isTrue()
        unregisterLambda.assertions().isCalledExactly(2)
        // Registration is not called twice
        registerWithLambda.assertions().isCalledOnce()
        onServiceUnregisteredResult.assertions().isCalledOnce().with(value(A_SESSION_ID))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `handle returns success if can register again, but after 2 distant removals user is not notified`() = runTest {
        val onServiceUnregisteredResult = lambdaRecorder<UserId, Unit> { }
        val unregisterLambda = lambdaRecorder<MatrixClient, String, Boolean, Result<Unit>> { _, _, _ -> Result.success(Unit) }
        val registerWithLambda = lambdaRecorder<MatrixClient, PushProvider, Distributor, Result<Unit>> { _, _, _ -> Result.success(Unit) }
        val sut = createDefaultUnifiedPushRemovedGatewayHandler(
            pushClientSecret = FakePushClientSecret(
                getUserIdFromSecretResult = { A_SESSION_ID },
            ),
            matrixClientProvider = FakeMatrixClientProvider(
                getClient = { Result.success(FakeMatrixClient()) },
            ),
            unregisterUnifiedPushUseCase = FakeUnregisterUnifiedPushUseCase(
                unregisterLambda = unregisterLambda,
            ),
            pushService = FakePushService(
                currentPushProvider = {
                    FakePushProvider(
                        currentDistributor = { Distributor("aValue", "aName") },
                    )
                },
                registerWithLambda = registerWithLambda,
                onServiceUnregisteredResult = onServiceUnregisteredResult,
            ),
        )
        val result = sut.handle(A_SECRET)
        assertThat(result.isSuccess).isTrue()
        unregisterLambda.assertions().isCalledOnce().with(
            any(),
            value(A_SECRET),
            value(false),
        )
        registerWithLambda.assertions().isCalledOnce()
        onServiceUnregisteredResult.assertions().isNeverCalled()
        // Second attempt in more than 1 minute
        advanceTimeBy(61.seconds)
        val result2 = sut.handle(A_SECRET)
        assertThat(result2.isSuccess).isTrue()
        unregisterLambda.assertions().isCalledExactly(2)
        // Registration is not called twice
        registerWithLambda.assertions().isCalledExactly(2)
        onServiceUnregisteredResult.assertions().isNeverCalled()
    }

    private fun TestScope.createDefaultUnifiedPushRemovedGatewayHandler(
        unregisterUnifiedPushUseCase: UnregisterUnifiedPushUseCase = FakeUnregisterUnifiedPushUseCase(),
        pushClientSecret: PushClientSecret = FakePushClientSecret(),
        matrixClientProvider: MatrixClientProvider = FakeMatrixClientProvider(),
        pushService: PushService = FakePushService(),
    ) = DefaultUnifiedPushRemovedGatewayHandler(
        unregisterUnifiedPushUseCase = unregisterUnifiedPushUseCase,
        pushClientSecret = pushClientSecret,
        matrixClientProvider = matrixClientProvider,
        pushService = pushService,
        unifiedPushRemovedGatewayThrottler = UnifiedPushRemovedGatewayThrottler(
            appCoroutineScope = backgroundScope,
        ),
    )
}
