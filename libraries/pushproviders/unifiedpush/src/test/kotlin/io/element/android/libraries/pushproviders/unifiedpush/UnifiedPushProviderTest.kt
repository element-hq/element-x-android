/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.test.aSessionPushConfig
import io.element.android.libraries.pushproviders.unifiedpush.troubleshoot.FakeUnifiedPushDistributorProvider
import io.element.android.libraries.pushproviders.unifiedpush.troubleshoot.FakeUnifiedPushSessionPushConfigProvider
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import io.element.android.libraries.pushstore.test.userpushstore.clientsecret.FakePushClientSecret
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
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
            .isCalledOnce()
            .with(value(A_SESSION_ID))
        executeLambda.assertions()
            .isCalledOnce()
            .with(value(Distributor("value", "Name")), value(A_SECRET))
        setDistributorValueResultLambda.assertions()
            .isCalledOnce()
            .with(value(A_SESSION_ID), value("value"))
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
            .isCalledOnce()
            .with(value(A_SESSION_ID))
        executeLambda.assertions()
            .isCalledOnce()
            .with(value(Distributor("value", "Name")), value(A_SECRET))
    }

    @Test
    fun `unregister ok`() = runTest {
        val matrixClient = FakeMatrixClient()
        val getSecretForUserResultLambda = lambdaRecorder<SessionId, String> { A_SECRET }
        val unregisterLambda = lambdaRecorder<MatrixClient, String, Boolean, Result<Unit>> { _, _, _ -> Result.success(Unit) }
        val unifiedPushProvider = createUnifiedPushProvider(
            pushClientSecret = FakePushClientSecret(
                getSecretForUserResult = getSecretForUserResultLambda,
            ),
            unRegisterUnifiedPushUseCase = FakeUnregisterUnifiedPushUseCase(
                unregisterLambda = unregisterLambda,
            ),
        )
        val result = unifiedPushProvider.unregister(matrixClient)
        assertThat(result).isEqualTo(Result.success(Unit))
        getSecretForUserResultLambda.assertions()
            .isCalledOnce()
            .with(value(A_SESSION_ID))
        unregisterLambda.assertions()
            .isCalledOnce()
            .with(value(matrixClient), value(A_SECRET), value(true))
    }

    @Test
    fun `unregister ko`() = runTest {
        val matrixClient = FakeMatrixClient()
        val getSecretForUserResultLambda = lambdaRecorder<SessionId, String> { A_SECRET }
        val unregisterLambda = lambdaRecorder<MatrixClient, String, Boolean, Result<Unit>> { _, _, _ -> Result.failure(AN_EXCEPTION) }
        val unifiedPushProvider = createUnifiedPushProvider(
            pushClientSecret = FakePushClientSecret(
                getSecretForUserResult = getSecretForUserResultLambda,
            ),
            unRegisterUnifiedPushUseCase = FakeUnregisterUnifiedPushUseCase(
                unregisterLambda = unregisterLambda,
            ),
        )
        val result = unifiedPushProvider.unregister(matrixClient)
        assertThat(result).isEqualTo(Result.failure<Unit>(AN_EXCEPTION))
        getSecretForUserResultLambda.assertions()
            .isCalledOnce()
            .with(value(A_SESSION_ID))
        unregisterLambda.assertions()
            .isCalledOnce()
            .with(value(matrixClient), value(A_SECRET), value(true))
    }

    @Test
    fun `getCurrentDistributor ok`() = runTest {
        val distributor = Distributor("value", "Name")
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
        val result = unifiedPushProvider.getCurrentDistributor(A_SESSION_ID)
        assertThat(result).isEqualTo(distributor)
    }

    @Test
    fun `getCurrentDistributor not know`() = runTest {
        val distributor = Distributor("value", "Name")
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
        val result = unifiedPushProvider.getCurrentDistributor(A_SESSION_ID)
        assertThat(result).isNull()
    }

    @Test
    fun `getCurrentDistributor not found`() = runTest {
        val distributor = Distributor("value", "Name")
        val unifiedPushProvider = createUnifiedPushProvider(
            unifiedPushStore = FakeUnifiedPushStore(
                getDistributorValueResult = { distributor.value }
            ),
            unifiedPushDistributorProvider = FakeUnifiedPushDistributorProvider(
                getDistributorsResult = emptyList()
            )
        )
        val result = unifiedPushProvider.getCurrentDistributor(A_SESSION_ID)
        assertThat(result).isNull()
    }

    @Test
    fun `getCurrentUserPushConfig invokes the provider methods`() = runTest {
        val currentUserPushConfig = aSessionPushConfig()
        val unifiedPushProvider = createUnifiedPushProvider(
            unifiedPushSessionPushConfigProvider = FakeUnifiedPushSessionPushConfigProvider(
                config = { currentUserPushConfig }
            )
        )
        val result = unifiedPushProvider.getPushConfig(A_SESSION_ID)
        assertThat(result).isEqualTo(currentUserPushConfig)
    }

    @Test
    fun `canRotateToken should return false`() = runTest {
        val unifiedPushProvider = createUnifiedPushProvider()
        assertThat(unifiedPushProvider.canRotateToken()).isFalse()
    }

    @Test
    fun `onSessionDeleted should do the cleanup`() = runTest {
        val cleanupLambda = lambdaRecorder<String, Boolean, Unit> { _, _ -> }
        val unifiedPushProvider = createUnifiedPushProvider(
            pushClientSecret = FakePushClientSecret(
                getSecretForUserResult = { A_SECRET }
            ),
            unRegisterUnifiedPushUseCase = FakeUnregisterUnifiedPushUseCase(
                cleanupLambda = cleanupLambda,
            ),
        )
        unifiedPushProvider.onSessionDeleted(A_SESSION_ID)
        cleanupLambda.assertions().isCalledOnce().with(value(A_SECRET), value(true))
    }

    private fun createUnifiedPushProvider(
        unifiedPushDistributorProvider: UnifiedPushDistributorProvider = FakeUnifiedPushDistributorProvider(),
        registerUnifiedPushUseCase: RegisterUnifiedPushUseCase = FakeRegisterUnifiedPushUseCase(),
        unRegisterUnifiedPushUseCase: UnregisterUnifiedPushUseCase = FakeUnregisterUnifiedPushUseCase(),
        pushClientSecret: PushClientSecret = FakePushClientSecret(),
        unifiedPushStore: UnifiedPushStore = FakeUnifiedPushStore(),
        unifiedPushSessionPushConfigProvider: UnifiedPushSessionPushConfigProvider = FakeUnifiedPushSessionPushConfigProvider(),
    ): UnifiedPushProvider {
        return UnifiedPushProvider(
            unifiedPushDistributorProvider = unifiedPushDistributorProvider,
            registerUnifiedPushUseCase = registerUnifiedPushUseCase,
            unRegisterUnifiedPushUseCase = unRegisterUnifiedPushUseCase,
            pushClientSecret = pushClientSecret,
            unifiedPushStore = unifiedPushStore,
            unifiedPushSessionPushConfigProvider = unifiedPushSessionPushConfigProvider,
        )
    }
}
