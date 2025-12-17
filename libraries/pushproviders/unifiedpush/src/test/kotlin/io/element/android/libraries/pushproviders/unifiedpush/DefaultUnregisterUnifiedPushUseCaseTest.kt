/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_SECRET
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.push.test.FakePusherSubscriber
import io.element.android.libraries.pushproviders.api.PusherSubscriber
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DefaultUnregisterUnifiedPushUseCaseTest {
    @Test
    fun `test un registration successful`() = runTest {
        val lambda = lambdaRecorder { _: MatrixClient, _: String, _: String -> Result.success(Unit) }
        val storeUpEndpointResult = lambdaRecorder { _: String, _: String? -> }
        val storePushGatewayResult = lambdaRecorder { _: String, _: String? -> }
        val matrixClient = FakeMatrixClient()
        val useCase = createDefaultUnregisterUnifiedPushUseCase(
            unifiedPushStore = FakeUnifiedPushStore(
                getEndpointResult = { "aEndpoint" },
                getPushGatewayResult = { "aGateway" },
                storeUpEndpointResult = storeUpEndpointResult,
                storePushGatewayResult = storePushGatewayResult,
            ),
            pusherSubscriber = FakePusherSubscriber(
                unregisterPusherResult = lambda
            )
        )
        val result = useCase.unregister(matrixClient, A_SECRET)
        assertThat(result.isSuccess).isTrue()
        lambda.assertions()
            .isCalledOnce()
            .with(value(matrixClient), value("aEndpoint"), value("aGateway"))
        storeUpEndpointResult.assertions()
            .isCalledOnce()
            .with(value(A_SECRET), value(null))
        storePushGatewayResult.assertions()
            .isCalledOnce()
            .with(value(A_SECRET), value(null))
    }

    @Test
    fun `test un registration error - no endpoint - will not unregister but return success`() = runTest {
        val matrixClient = FakeMatrixClient()
        val storeUpEndpointResult = lambdaRecorder { _: String, _: String? -> }
        val storePushGatewayResult = lambdaRecorder { _: String, _: String? -> }
        val useCase = createDefaultUnregisterUnifiedPushUseCase(
            unifiedPushStore = FakeUnifiedPushStore(
                getEndpointResult = { null },
                getPushGatewayResult = { "aGateway" },
                storeUpEndpointResult = storeUpEndpointResult,
                storePushGatewayResult = storePushGatewayResult,
            ),
        )
        val result = useCase.unregister(matrixClient, A_SECRET)
        assertThat(result.isSuccess).isTrue()
        storeUpEndpointResult.assertions()
            .isCalledOnce()
            .with(value(A_SECRET), value(null))
        storePushGatewayResult.assertions()
            .isCalledOnce()
            .with(value(A_SECRET), value(null))
    }

    @Test
    fun `test un registration error - no gateway - will not unregister but return success`() = runTest {
        val matrixClient = FakeMatrixClient()
        val storeUpEndpointResult = lambdaRecorder { _: String, _: String? -> }
        val storePushGatewayResult = lambdaRecorder { _: String, _: String? -> }
        val useCase = createDefaultUnregisterUnifiedPushUseCase(
            unifiedPushStore = FakeUnifiedPushStore(
                getEndpointResult = { "aEndpoint" },
                getPushGatewayResult = { null },
                storeUpEndpointResult = storeUpEndpointResult,
                storePushGatewayResult = storePushGatewayResult,
            ),
        )
        val result = useCase.unregister(matrixClient, A_SECRET)
        assertThat(result.isSuccess).isTrue()
        storeUpEndpointResult.assertions()
            .isCalledOnce()
            .with(value(A_SECRET), value(null))
        storePushGatewayResult.assertions()
            .isCalledOnce()
            .with(value(A_SECRET), value(null))
    }

    @Test
    fun `test un registration error`() = runTest {
        val matrixClient = FakeMatrixClient()
        val useCase = createDefaultUnregisterUnifiedPushUseCase(
            unifiedPushStore = FakeUnifiedPushStore(
                getEndpointResult = { "aEndpoint" },
                getPushGatewayResult = { "aGateway" },
            ),
            pusherSubscriber = FakePusherSubscriber(
                unregisterPusherResult = { _, _, _ -> Result.failure(AN_EXCEPTION) }
            )
        )
        val result = useCase.unregister(matrixClient, A_SECRET)
        assertThat(result.isFailure).isTrue()
    }

    private fun createDefaultUnregisterUnifiedPushUseCase(
        unifiedPushStore: UnifiedPushStore = FakeUnifiedPushStore(),
        pusherSubscriber: PusherSubscriber = FakePusherSubscriber()
    ): DefaultUnregisterUnifiedPushUseCase {
        val context = InstrumentationRegistry.getInstrumentation().context
        return DefaultUnregisterUnifiedPushUseCase(
            context = context,
            unifiedPushStore = unifiedPushStore,
            pusherSubscriber = pusherSubscriber
        )
    }
}
