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

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_SECRET
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.pushproviders.api.CurrentUserPushConfig
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.unifiedpush.troubleshoot.FakeUnifiedPushDistributorProvider
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import io.element.android.libraries.pushstore.test.userpushstore.clientsecret.FakePushClientSecret
import io.element.android.services.appnavstate.api.AppNavigationState
import io.element.android.services.appnavstate.api.AppNavigationStateService
import io.element.android.services.appnavstate.api.NavigationState
import io.element.android.services.appnavstate.test.FakeAppNavigationStateService
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class UnifiedPushProviderTest {
    @Test
    fun `test index and name`() {
        val unifiedPushProvider = createUnifiedPushProvider()
        assertThat(unifiedPushProvider.name).isEqualTo(UnifiedPushConfig.NAME)
        assertThat(unifiedPushProvider.index).isEqualTo(UnifiedPushConfig.INDEX)
    }

    @Test
    fun `getDistributors return the available distributors`() {
        val unifiedPushProvider = createUnifiedPushProvider(
            unifiedPushDistributorProvider = FakeUnifiedPushDistributorProvider(
                getDistributorsResult = listOf(
                    Distributor("value", "Name"),
                )
            )
        )
        val result = unifiedPushProvider.getDistributors()
        assertThat(result).containsExactly(Distributor("value", "Name"))
        assertThat(unifiedPushProvider.isAvailable()).isTrue()
    }

    @Test
    fun `getDistributors return empty`() {
        val unifiedPushProvider = createUnifiedPushProvider(
            unifiedPushDistributorProvider = FakeUnifiedPushDistributorProvider(
                getDistributorsResult = emptyList()
            )
        )
        val result = unifiedPushProvider.getDistributors()
        assertThat(result).isEmpty()
        assertThat(unifiedPushProvider.isAvailable()).isFalse()
    }

    @Test
    fun `register ok`() = runTest {
        val getSecretForUserResultLambda = lambdaRecorder<SessionId, String> { A_SECRET }
        val executeLambda = lambdaRecorder<Distributor, String, Result<Unit>> { _, _ -> Result.success(Unit) }
        val setDistributorValueResultLambda = lambdaRecorder<UserId, String, Unit> { _, _ -> }
        val unifiedPushProvider = createUnifiedPushProvider(
            pushClientSecret = FakePushClientSecret(
                getSecretForUserResult = getSecretForUserResultLambda,
            ),
            registerUnifiedPushUseCase = FakeRegisterUnifiedPushUseCase(
                result = executeLambda,
            ),
            unifiedPushStore = FakeUnifiedPushStore(
                setDistributorValueResult = setDistributorValueResultLambda,
            ),
        )
        val result = unifiedPushProvider.registerWith(FakeMatrixClient(), Distributor("value", "Name"))
        assertThat(result).isEqualTo(Result.success(Unit))
        getSecretForUserResultLambda.assertions()
            .isCalledExactly(1)
            .withSequence(listOf(value(A_SESSION_ID)))
        executeLambda.assertions()
            .isCalledExactly(1)
            .withSequence(listOf(value(Distributor("value", "Name")), value(A_SECRET)))
        setDistributorValueResultLambda.assertions()
            .isCalledExactly(1)
            .withSequence(listOf(value(A_SESSION_ID), value("value")))
    }

    @Test
    fun `register ko`() = runTest {
        val getSecretForUserResultLambda = lambdaRecorder<SessionId, String> { A_SECRET }
        val executeLambda = lambdaRecorder<Distributor, String, Result<Unit>> { _, _ -> Result.failure(AN_EXCEPTION) }
        val setDistributorValueResultLambda = lambdaRecorder<UserId, String, Unit>(ensureNeverCalled = true) { _, _ -> }
        val unifiedPushProvider = createUnifiedPushProvider(
            pushClientSecret = FakePushClientSecret(
                getSecretForUserResult = getSecretForUserResultLambda,
            ),
            registerUnifiedPushUseCase = FakeRegisterUnifiedPushUseCase(
                result = executeLambda,
            ),
            unifiedPushStore = FakeUnifiedPushStore(
                setDistributorValueResult = setDistributorValueResultLambda,
            ),
        )
        val result = unifiedPushProvider.registerWith(FakeMatrixClient(), Distributor("value", "Name"))
        assertThat(result).isEqualTo(Result.failure<Unit>(AN_EXCEPTION))
        getSecretForUserResultLambda.assertions()
            .isCalledExactly(1)
            .withSequence(listOf(value(A_SESSION_ID)))
        executeLambda.assertions()
            .isCalledExactly(1)
            .withSequence(listOf(value(Distributor("value", "Name")), value(A_SECRET)))
    }

    @Test
    fun `unregister ok`() = runTest {
        val matrixClient = FakeMatrixClient()
        val getSecretForUserResultLambda = lambdaRecorder<SessionId, String> { A_SECRET }
        val executeLambda = lambdaRecorder<MatrixClient, String, Result<Unit>> { _, _ -> Result.success(Unit) }
        val unifiedPushProvider = createUnifiedPushProvider(
            pushClientSecret = FakePushClientSecret(
                getSecretForUserResult = getSecretForUserResultLambda,
            ),
            unRegisterUnifiedPushUseCase = FakeUnregisterUnifiedPushUseCase(
                result = executeLambda,
            ),
        )
        val result = unifiedPushProvider.unregister(matrixClient)
        assertThat(result).isEqualTo(Result.success(Unit))
        getSecretForUserResultLambda.assertions()
            .isCalledExactly(1)
            .withSequence(listOf(value(A_SESSION_ID)))
        executeLambda.assertions()
            .isCalledExactly(1)
            .withSequence(listOf(value(matrixClient), value(A_SECRET)))
    }

    @Test
    fun `unregister ko`() = runTest {
        val matrixClient = FakeMatrixClient()
        val getSecretForUserResultLambda = lambdaRecorder<SessionId, String> { A_SECRET }
        val executeLambda = lambdaRecorder<MatrixClient, String, Result<Unit>> { _, _ -> Result.failure(AN_EXCEPTION) }
        val unifiedPushProvider = createUnifiedPushProvider(
            pushClientSecret = FakePushClientSecret(
                getSecretForUserResult = getSecretForUserResultLambda,
            ),
            unRegisterUnifiedPushUseCase = FakeUnregisterUnifiedPushUseCase(
                result = executeLambda,
            ),
        )
        val result = unifiedPushProvider.unregister(matrixClient)
        assertThat(result).isEqualTo(Result.failure<Unit>(AN_EXCEPTION))
        getSecretForUserResultLambda.assertions()
            .isCalledExactly(1)
            .withSequence(listOf(value(A_SESSION_ID)))
        executeLambda.assertions()
            .isCalledExactly(1)
            .withSequence(listOf(value(matrixClient), value(A_SECRET)))
    }

    @Test
    fun `getCurrentDistributor ok`() = runTest {
        val distributor = Distributor("value", "Name")
        val matrixClient = FakeMatrixClient()
        val unifiedPushProvider = createUnifiedPushProvider(
            unifiedPushStore = FakeUnifiedPushStore(
                getDistributorValueResult = { distributor.value }
            ),
            unifiedPushDistributorProvider = FakeUnifiedPushDistributorProvider(
                getDistributorsResult = listOf(
                    Distributor("value2", "Name2"),
                    distributor,
                )
            )
        )
        val result = unifiedPushProvider.getCurrentDistributor(matrixClient)
        assertThat(result).isEqualTo(distributor)
    }

    @Test
    fun `getCurrentDistributor not know`() = runTest {
        val distributor = Distributor("value", "Name")
        val matrixClient = FakeMatrixClient()
        val unifiedPushProvider = createUnifiedPushProvider(
            unifiedPushStore = FakeUnifiedPushStore(
                getDistributorValueResult = { "unknown" }
            ),
            unifiedPushDistributorProvider = FakeUnifiedPushDistributorProvider(
                getDistributorsResult = listOf(
                    distributor,
                )
            )
        )
        val result = unifiedPushProvider.getCurrentDistributor(matrixClient)
        assertThat(result).isNull()
    }

    @Test
    fun `getCurrentDistributor not found`() = runTest {
        val distributor = Distributor("value", "Name")
        val matrixClient = FakeMatrixClient()
        val unifiedPushProvider = createUnifiedPushProvider(
            unifiedPushStore = FakeUnifiedPushStore(
                getDistributorValueResult = { distributor.value }
            ),
            unifiedPushDistributorProvider = FakeUnifiedPushDistributorProvider(
                getDistributorsResult = emptyList()
            )
        )
        val result = unifiedPushProvider.getCurrentDistributor(matrixClient)
        assertThat(result).isNull()
    }

    @Test
    fun `getCurrentUserPushConfig no session`() = runTest {
        val unifiedPushProvider = createUnifiedPushProvider()
        val result = unifiedPushProvider.getCurrentUserPushConfig()
        assertThat(result).isNull()
    }

    @Test
    fun `getCurrentUserPushConfig no push gateway`() = runTest {
        val unifiedPushProvider = createUnifiedPushProvider(
            appNavigationStateService = FakeAppNavigationStateService(
                appNavigationState = MutableStateFlow(
                    AppNavigationState(
                        navigationState = NavigationState.Session(owner = "owner", sessionId = A_SESSION_ID),
                        isInForeground = true
                    )
                )
            ),
            pushClientSecret = FakePushClientSecret(
                getSecretForUserResult = { A_SECRET }
            ),
            unifiedPushStore = FakeUnifiedPushStore(
                getPushGatewayResult = { null }
            ),
        )
        val result = unifiedPushProvider.getCurrentUserPushConfig()
        assertThat(result).isNull()
    }

    @Test
    fun `getCurrentUserPushConfig no push key`() = runTest {
        val unifiedPushProvider = createUnifiedPushProvider(
            appNavigationStateService = FakeAppNavigationStateService(
                appNavigationState = MutableStateFlow(
                    AppNavigationState(
                        navigationState = NavigationState.Session(owner = "owner", sessionId = A_SESSION_ID),
                        isInForeground = true
                    )
                )
            ),
            pushClientSecret = FakePushClientSecret(
                getSecretForUserResult = { A_SECRET }
            ),
            unifiedPushStore = FakeUnifiedPushStore(
                getPushGatewayResult = { "aPushGateway" },
                getEndpointResult = { null }
            ),
        )
        val result = unifiedPushProvider.getCurrentUserPushConfig()
        assertThat(result).isNull()
    }

    @Test
    fun `getCurrentUserPushConfig ok`() = runTest {
        val unifiedPushProvider = createUnifiedPushProvider(
            appNavigationStateService = FakeAppNavigationStateService(
                appNavigationState = MutableStateFlow(
                    AppNavigationState(
                        navigationState = NavigationState.Session(owner = "owner", sessionId = A_SESSION_ID),
                        isInForeground = true
                    )
                )
            ),
            pushClientSecret = FakePushClientSecret(
                getSecretForUserResult = { A_SECRET }
            ),
            unifiedPushStore = FakeUnifiedPushStore(
                getPushGatewayResult = { "aPushGateway" },
                getEndpointResult = { "aEndpoint" }
            ),
        )
        val result = unifiedPushProvider.getCurrentUserPushConfig()
        assertThat(result).isEqualTo(CurrentUserPushConfig("aPushGateway", "aEndpoint"))
    }

    private fun createUnifiedPushProvider(
        unifiedPushDistributorProvider: UnifiedPushDistributorProvider = FakeUnifiedPushDistributorProvider(),
        registerUnifiedPushUseCase: RegisterUnifiedPushUseCase = FakeRegisterUnifiedPushUseCase(),
        unRegisterUnifiedPushUseCase: UnregisterUnifiedPushUseCase = FakeUnregisterUnifiedPushUseCase(),
        pushClientSecret: PushClientSecret = FakePushClientSecret(),
        unifiedPushStore: UnifiedPushStore = FakeUnifiedPushStore(),
        appNavigationStateService: AppNavigationStateService = FakeAppNavigationStateService(),
    ): UnifiedPushProvider {
        return UnifiedPushProvider(
            unifiedPushDistributorProvider = unifiedPushDistributorProvider,
            registerUnifiedPushUseCase = registerUnifiedPushUseCase,
            unRegisterUnifiedPushUseCase = unRegisterUnifiedPushUseCase,
            pushClientSecret = pushClientSecret,
            unifiedPushStore = unifiedPushStore,
            appNavigationStateService = appNavigationStateService
        )
    }
}
