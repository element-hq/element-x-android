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
        val storeUpEndpointResult = lambdaRecorder { _: String?, _: String -> }
        val storePushGatewayResult = lambdaRecorder { _: String?, _: String -> }
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
        val result = useCase.execute(matrixClient, A_SECRET)
        assertThat(result.isSuccess).isTrue()
        lambda.assertions()
            .isCalledOnce()
            .with(value(matrixClient), value("aEndpoint"), value("aGateway"))
        storeUpEndpointResult.assertions()
            .isCalledOnce()
            .with(value(null), value(A_SECRET))
        storePushGatewayResult.assertions()
            .isCalledOnce()
            .with(value(null), value(A_SECRET))
    }

    @Test
    fun `test un registration error - no endpoint`() = runTest {
        val matrixClient = FakeMatrixClient()
        val useCase = createDefaultUnregisterUnifiedPushUseCase(
            unifiedPushStore = FakeUnifiedPushStore(
                getEndpointResult = { null },
                getPushGatewayResult = { "aGateway" },
            ),
        )
        val result = useCase.execute(matrixClient, A_SECRET)
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `test un registration error - no gateway`() = runTest {
        val matrixClient = FakeMatrixClient()
        val useCase = createDefaultUnregisterUnifiedPushUseCase(
            unifiedPushStore = FakeUnifiedPushStore(
                getEndpointResult = { "aEndpoint" },
                getPushGatewayResult = { null },
            ),
        )
        val result = useCase.execute(matrixClient, A_SECRET)
        assertThat(result.isFailure).isTrue()
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
        val result = useCase.execute(matrixClient, A_SECRET)
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
