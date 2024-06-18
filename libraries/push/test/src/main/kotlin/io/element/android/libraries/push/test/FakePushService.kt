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

package io.element.android.libraries.push.test

import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.api.PushService
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.simulateLongTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePushService(
    private val testPushBlock: suspend () -> Boolean = { true },
    private val availablePushProviders: List<PushProvider> = emptyList(),
    private val registerWithLambda: suspend (MatrixClient, PushProvider, Distributor) -> Result<Unit> = { _, _, _ ->
        Result.success(Unit)
    },
    private val currentPushProvider: () -> PushProvider? = { availablePushProviders.firstOrNull() },
    private val selectPushProviderLambda: suspend (MatrixClient, PushProvider) -> Unit = { _, _ -> lambdaError() },
    private val setIgnoreRegistrationErrorLambda: (SessionId, Boolean) -> Unit = { _, _ -> lambdaError() },
) : PushService {
    override suspend fun getCurrentPushProvider(): PushProvider? {
        return registeredPushProvider ?: currentPushProvider()
    }

    override fun getAvailablePushProviders(): List<PushProvider> {
        return availablePushProviders
    }

    private var registeredPushProvider: PushProvider? = null

    override suspend fun registerWith(
        matrixClient: MatrixClient,
        pushProvider: PushProvider,
        distributor: Distributor,
    ): Result<Unit> = simulateLongTask {
        return registerWithLambda(matrixClient, pushProvider, distributor)
            .also {
                if (it.isSuccess) {
                    registeredPushProvider = pushProvider
                }
            }
    }

    override suspend fun selectPushProvider(matrixClient: MatrixClient, pushProvider: PushProvider) {
        selectPushProviderLambda(matrixClient, pushProvider)
    }

    private val ignoreRegistrationError = MutableStateFlow(false)

    override fun ignoreRegistrationError(sessionId: SessionId): Flow<Boolean> {
        return ignoreRegistrationError
    }

    override suspend fun setIgnoreRegistrationError(sessionId: SessionId, ignore: Boolean) {
        ignoreRegistrationError.value = ignore
        setIgnoreRegistrationErrorLambda(sessionId, ignore)
    }

    override suspend fun testPush(): Boolean = simulateLongTask {
        testPushBlock()
    }
}
