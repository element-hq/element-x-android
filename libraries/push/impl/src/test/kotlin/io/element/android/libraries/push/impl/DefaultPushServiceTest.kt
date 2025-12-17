/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.verification.FakeSessionVerificationService
import io.element.android.libraries.push.api.GetCurrentPushProvider
import io.element.android.libraries.push.api.PusherRegistrationFailure
import io.element.android.libraries.push.api.history.PushHistoryItem
import io.element.android.libraries.push.impl.push.FakeMutableBatteryOptimizationStore
import io.element.android.libraries.push.impl.push.MutableBatteryOptimizationStore
import io.element.android.libraries.push.impl.store.InMemoryPushDataStore
import io.element.android.libraries.push.impl.store.PushDataStore
import io.element.android.libraries.push.impl.test.FakeTestPush
import io.element.android.libraries.push.impl.test.TestPush
import io.element.android.libraries.push.impl.unregistration.FakeServiceUnregisteredHandler
import io.element.android.libraries.push.impl.unregistration.ServiceUnregisteredHandler
import io.element.android.libraries.push.test.FakeGetCurrentPushProvider
import io.element.android.libraries.pushproviders.api.Config
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.libraries.pushproviders.test.FakePushProvider
import io.element.android.libraries.pushproviders.test.aSessionPushConfig
import io.element.android.libraries.pushstore.api.UserPushStoreFactory
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecretStore
import io.element.android.libraries.pushstore.test.userpushstore.FakeUserPushStore
import io.element.android.libraries.pushstore.test.userpushstore.FakeUserPushStoreFactory
import io.element.android.libraries.pushstore.test.userpushstore.clientsecret.InMemoryPushClientSecretStore
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver
import io.element.android.libraries.sessionstorage.test.observer.NoOpSessionObserver
import io.element.android.tests.testutils.lambda.any
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultPushServiceTest {
    @Test
    fun `test push no push provider`() = runTest {
        val defaultPushService = createDefaultPushService()
        assertThat(defaultPushService.testPush(A_SESSION_ID)).isFalse()
    }

    @Test
    fun `test push no config`() = runTest {
        val aPushProvider = FakePushProvider()
        val defaultPushService = createDefaultPushService(
            pushProviders = setOf(aPushProvider),
            getCurrentPushProvider = FakeGetCurrentPushProvider(currentPushProvider = aPushProvider.name),
        )
        assertThat(defaultPushService.testPush(A_SESSION_ID)).isFalse()
    }

    @Test
    fun `test push ok`() = runTest {
        val aConfig = aSessionPushConfig()
        val testPushResult = lambdaRecorder<Config, Unit> { }
        val aPushProvider = FakePushProvider(
            config = aConfig
        )
        val defaultPushService = createDefaultPushService(
            pushProviders = setOf(aPushProvider),
            getCurrentPushProvider = FakeGetCurrentPushProvider(currentPushProvider = aPushProvider.name),
            testPush = FakeTestPush(executeResult = testPushResult),
        )
        assertThat(defaultPushService.testPush(A_SESSION_ID)).isTrue()
        testPushResult.assertions()
            .isCalledOnce()
            .with(value(aConfig))
    }

    @Test
    fun `getCurrentPushProvider null`() = runTest {
        val defaultPushService = createDefaultPushService()
        val result = defaultPushService.getCurrentPushProvider(A_SESSION_ID)
        assertThat(result).isNull()
    }

    @Test
    fun `getCurrentPushProvider ok`() = runTest {
        val aPushProvider = FakePushProvider()
        val defaultPushService = createDefaultPushService(
            pushProviders = setOf(aPushProvider),
            getCurrentPushProvider = FakeGetCurrentPushProvider(currentPushProvider = aPushProvider.name),
        )
        val result = defaultPushService.getCurrentPushProvider(A_SESSION_ID)
        assertThat(result).isEqualTo(aPushProvider)
    }

    @Test
    fun `getAvailablePushProviders empty`() = runTest {
        val defaultPushService = createDefaultPushService()
        val result = defaultPushService.getAvailablePushProviders()
        assertThat(result).isEmpty()
    }

    @Test
    fun `registerWith ok`() = runTest {
        val client = FakeMatrixClient()
        val aPushProvider = FakePushProvider(
            registerWithResult = { _, _ -> Result.success(Unit) },
        )
        val aDistributor = Distributor("aValue", "aName")
        val defaultPushService = createDefaultPushService()
        val result = defaultPushService.registerWith(client, aPushProvider, aDistributor)
        assertThat(result).isEqualTo(Result.success(Unit))
    }

    @Test
    fun `registerWith fail to register`() = runTest {
        val client = FakeMatrixClient()
        val aPushProvider = FakePushProvider(
            registerWithResult = { _, _ -> Result.failure(AN_EXCEPTION) },
        )
        val aDistributor = Distributor("aValue", "aName")
        val defaultPushService = createDefaultPushService()
        val result = defaultPushService.registerWith(client, aPushProvider, aDistributor)
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `registerWith fail to unregister previous push provider`() = runTest {
        val client = FakeMatrixClient()
        val aCurrentPushProvider = FakePushProvider(
            unregisterWithResult = { Result.failure(AN_EXCEPTION) },
            name = "aCurrentPushProvider",
        )
        val aPushProvider = FakePushProvider(
            name = "aPushProvider",
        )
        val userPushStore = FakeUserPushStore().apply {
            setPushProviderName(aCurrentPushProvider.name)
        }
        val aDistributor = Distributor("aValue", "aName")
        val defaultPushService = createDefaultPushService(
            pushProviders = setOf(aCurrentPushProvider, aPushProvider),
            getCurrentPushProvider = FakeGetCurrentPushProvider(currentPushProvider = aCurrentPushProvider.name),
            userPushStoreFactory = FakeUserPushStoreFactory(
                userPushStore = { userPushStore },
            ),
        )
        val result = defaultPushService.registerWith(client, aPushProvider, aDistributor)
        assertThat(result.isFailure).isTrue()
        assertThat(userPushStore.getPushProviderName()).isEqualTo(aCurrentPushProvider.name)
    }

    @Test
    fun `registerWith unregister previous push provider and register new OK`() = runTest {
        val client = FakeMatrixClient()
        val unregisterLambda = lambdaRecorder<MatrixClient, Result<Unit>> { Result.success(Unit) }
        val registerLambda = lambdaRecorder<MatrixClient, Distributor, Result<Unit>> { _, _ -> Result.success(Unit) }
        val aCurrentPushProvider = FakePushProvider(
            unregisterWithResult = unregisterLambda,
            name = "aCurrentPushProvider",
        )
        val aPushProvider = FakePushProvider(
            registerWithResult = registerLambda,
            name = "aPushProvider",
        )
        val userPushStore = FakeUserPushStore().apply {
            setPushProviderName(aCurrentPushProvider.name)
        }
        val aDistributor = Distributor("aValue", "aName")
        val defaultPushService = createDefaultPushService(
            pushProviders = setOf(aCurrentPushProvider, aPushProvider),
            getCurrentPushProvider = FakeGetCurrentPushProvider(currentPushProvider = aCurrentPushProvider.name),
            userPushStoreFactory = FakeUserPushStoreFactory(
                userPushStore = { userPushStore },
            ),
        )
        val result = defaultPushService.registerWith(client, aPushProvider, aDistributor)
        assertThat(result.isSuccess).isTrue()
        assertThat(userPushStore.getPushProviderName()).isEqualTo(aPushProvider.name)
        unregisterLambda.assertions()
            .isCalledOnce()
            .with(value(client))
        registerLambda.assertions()
            .isCalledOnce()
            .with(value(client), value(aDistributor))
    }

    @Test
    fun `getAvailablePushProviders sorted`() = runTest {
        val aPushProvider1 = FakePushProvider(
            index = 1,
            name = "aPushProvider1",
        )
        val aPushProvider2 = FakePushProvider(
            index = 2,
            name = "aPushProvider2",
        )
        val aPushProvider3 = FakePushProvider(
            index = 3,
            name = "aPushProvider3",
        )
        val defaultPushService = createDefaultPushService(
            pushProviders = setOf(aPushProvider1, aPushProvider3, aPushProvider2),
        )
        val result = defaultPushService.getAvailablePushProviders()
        assertThat(result).containsExactly(aPushProvider1, aPushProvider2, aPushProvider3).inOrder()
    }

    @Test
    fun `test setIgnoreRegistrationError is sent to the store`() = runTest {
        val userPushStore = FakeUserPushStore().apply {
        }
        val defaultPushService = createDefaultPushService(
            userPushStoreFactory = FakeUserPushStoreFactory(
                userPushStore = { userPushStore },
            ),
        )
        assertThat(defaultPushService.ignoreRegistrationError(A_SESSION_ID).first()).isFalse()
        defaultPushService.setIgnoreRegistrationError(A_SESSION_ID, true)
        assertThat(defaultPushService.ignoreRegistrationError(A_SESSION_ID).first()).isTrue()
    }

    @Test
    fun `onSessionCreated is noop`() = runTest {
        val defaultPushService = createDefaultPushService()
        defaultPushService.onSessionCreated(A_SESSION_ID.value)
    }

    @Test
    fun `onSessionDeleted should transmit the info to the current push provider and cleanup the stores`() = runTest {
        val onSessionDeletedLambda = lambdaRecorder<SessionId, Unit> { }
        val aCurrentPushProvider = FakePushProvider(
            name = "aCurrentPushProvider",
            onSessionDeletedLambda = onSessionDeletedLambda,
        )
        val userPushStore = FakeUserPushStore(
            pushProviderName = aCurrentPushProvider.name,
        )
        val pushClientSecretStore = InMemoryPushClientSecretStore()
        val defaultPushService = createDefaultPushService(
            pushProviders = setOf(aCurrentPushProvider),
            getCurrentPushProvider = FakeGetCurrentPushProvider(currentPushProvider = aCurrentPushProvider.name),
            userPushStoreFactory = FakeUserPushStoreFactory(
                userPushStore = { userPushStore },
            ),
            pushClientSecretStore = pushClientSecretStore,
        )
        defaultPushService.onSessionDeleted(A_SESSION_ID.value, false)
        assertThat(userPushStore.getPushProviderName()).isNull()
        assertThat(pushClientSecretStore.getSecret(A_SESSION_ID)).isNull()
        onSessionDeletedLambda.assertions().isCalledOnce().with(value(A_SESSION_ID))
    }

    @Test
    fun `onSessionDeleted when there is no push provider should just cleanup the stores`() = runTest {
        val userPushStore = FakeUserPushStore(
            pushProviderName = null,
        )
        val pushClientSecretStore = InMemoryPushClientSecretStore()
        val defaultPushService = createDefaultPushService(
            pushProviders = emptySet(),
            getCurrentPushProvider = FakeGetCurrentPushProvider(currentPushProvider = null),
            userPushStoreFactory = FakeUserPushStoreFactory(
                userPushStore = { userPushStore },
            ),
            pushClientSecretStore = pushClientSecretStore,
        )
        defaultPushService.onSessionDeleted(A_SESSION_ID.value, false)
        assertThat(userPushStore.getPushProviderName()).isNull()
        assertThat(pushClientSecretStore.getSecret(A_SESSION_ID)).isNull()
    }

    @Test
    fun `selectPushProvider should store the data in the store`() = runTest {
        val userPushStore = FakeUserPushStore()
        val defaultPushService = createDefaultPushService(
            userPushStoreFactory = FakeUserPushStoreFactory(
                userPushStore = { userPushStore },
            ),
        )
        val aPushProvider = FakePushProvider(
            name = "aCurrentPushProvider",
        )
        assertThat(userPushStore.getPushProviderName()).isNull()
        defaultPushService.selectPushProvider(A_SESSION_ID, aPushProvider)
        assertThat(userPushStore.getPushProviderName()).isEqualTo(aPushProvider.name)
    }

    @Test
    fun `resetBatteryOptimizationState invokes the store method`() = runTest {
        val resetResult = lambdaRecorder<Unit> { }
        val defaultPushService = createDefaultPushService(
            mutableBatteryOptimizationStore = FakeMutableBatteryOptimizationStore(
                resetResult = resetResult,
            ),
        )
        defaultPushService.resetBatteryOptimizationState()
        resetResult.assertions().isCalledOnce()
    }

    @Test
    fun `resetPushHistory invokes the store method`() = runTest {
        val resetResult = lambdaRecorder<Unit> { }
        val defaultPushService = createDefaultPushService(
            pushDataStore = InMemoryPushDataStore(
                resetResult = resetResult
            ),
        )
        defaultPushService.resetPushHistory()
        resetResult.assertions().isCalledOnce()
    }

    @Test
    fun `getPushHistoryItemsFlow invokes the store method`() = runTest {
        val store = InMemoryPushDataStore()
        val aPushHistoryItem = PushHistoryItem(
            pushDate = 0L,
            formattedDate = "formattedDate",
            providerInfo = "providerInfo",
            eventId = AN_EVENT_ID,
            roomId = A_ROOM_ID,
            sessionId = A_SESSION_ID,
            hasBeenResolved = false,
            comment = null,
        )
        val defaultPushService = createDefaultPushService(
            pushDataStore = store,
        )
        defaultPushService.getPushHistoryItemsFlow().test {
            assertThat(awaitItem().isEmpty()).isTrue()
            store.emitPushHistoryItems(listOf(aPushHistoryItem))
            assertThat(awaitItem().first()).isEqualTo(aPushHistoryItem)
        }
    }

    @Test
    fun `ensurePusher - error when account is not verified`() = runTest {
        val sessionVerificationService = FakeSessionVerificationService(
            initialSessionVerifiedStatus = SessionVerifiedStatus.NotVerified
        )
        val pushService = createDefaultPushService()
        val result = pushService.ensurePusherIsRegistered(
            FakeMatrixClient(
                sessionVerificationService = sessionVerificationService,
            )
        )
        assertThat(result.exceptionOrNull()!!).isInstanceOf(PusherRegistrationFailure.AccountNotVerified::class.java)
    }

    @Test
    fun `ensurePusher - case two push providers but first one does not have distributor - second one will be used`() = runTest {
        val lambda = lambdaRecorder<MatrixClient, Distributor, Result<Unit>> { _, _ ->
            Result.success(Unit)
        }
        val sessionVerificationService = FakeSessionVerificationService(
            initialSessionVerifiedStatus = SessionVerifiedStatus.Verified
        )
        val pushProvider0 = FakePushProvider(
            index = 0,
            name = "aFakePushProvider0",
            distributors = emptyList(),
        )
        val distributor = Distributor("aDistributorValue1", "aDistributorName1")
        val pushProvider1 = FakePushProvider(
            index = 1,
            name = "aFakePushProvider1",
            distributors = listOf(distributor),
            registerWithResult = lambda,
        )
        val pushService = createDefaultPushService(
            pushProviders = setOf(
                pushProvider0,
                pushProvider1,
            ),
        )
        val result = pushService.ensurePusherIsRegistered(
            FakeMatrixClient(
                sessionVerificationService = sessionVerificationService,
            )
        )
        assertThat(result.isSuccess).isTrue()
        lambda.assertions().isCalledOnce()
            .with(
                // MatrixClient
                any(),
                // First distributor of second push provider
                value(distributor),
            )
    }

    @Test
    fun `ensurePusher - case one push provider but no distributor available`() = runTest {
        val lambda = lambdaRecorder<MatrixClient, Distributor, Result<Unit>> { _, _ ->
            Result.success(Unit)
        }
        val sessionVerificationService = FakeSessionVerificationService(
            initialSessionVerifiedStatus = SessionVerifiedStatus.Verified
        )
        val pushProvider = FakePushProvider(
            index = 0,
            name = "aFakePushProvider",
            distributors = emptyList(),
            registerWithResult = lambda,
        )
        val pushService = createDefaultPushService(
            pushProviders = setOf(pushProvider),
        )
        val result = pushService.ensurePusherIsRegistered(
            FakeMatrixClient(
                sessionVerificationService = sessionVerificationService,
            )
        )
        assertThat(result.exceptionOrNull()).isInstanceOf(PusherRegistrationFailure.NoDistributorsAvailable::class.java)
        lambda.assertions().isNeverCalled()
    }

    @Test
    fun `ensurePusher - ensure default pusher is registered with default provider`() = runTest {
        val lambda = lambdaRecorder<MatrixClient, Distributor, Result<Unit>> { _, _ ->
            Result.success(Unit)
        }
        val sessionVerificationService = FakeSessionVerificationService(
            initialSessionVerifiedStatus = SessionVerifiedStatus.Verified
        )
        val pushService = createDefaultPushService(
            pushProviders = setOf(
                FakePushProvider(
                    index = 0,
                    name = "aFakePushProvider",
                    distributors = listOf(Distributor("aDistributorValue0", "aDistributorName0")),
                    registerWithResult = lambda,
                )
            ),
        )
        val result = pushService.ensurePusherIsRegistered(
            FakeMatrixClient(
                sessionVerificationService = sessionVerificationService,
            )
        )
        assertThat(result.isSuccess).isTrue()
        lambda.assertions()
            .isCalledOnce()
            .with(
                // MatrixClient
                any(),
                // First distributor
                value(pushService.getAvailablePushProviders()[0].getDistributors()[0]),
            )
    }

    @Test
    fun `ensurePusher - ensure default pusher is registered with default provider - fail to register`() = runTest {
        val lambda = lambdaRecorder<MatrixClient, Distributor, Result<Unit>> { _, _ ->
            Result.failure(AN_EXCEPTION)
        }
        val sessionVerificationService = FakeSessionVerificationService(
            initialSessionVerifiedStatus = SessionVerifiedStatus.Verified
        )
        val pushService = createDefaultPushService(
            pushProviders = setOf(
                FakePushProvider(
                    index = 0,
                    name = "aFakePushProvider",
                    distributors = listOf(Distributor("aDistributorValue0", "aDistributorName0")),
                    registerWithResult = lambda,
                )
            ),
        )
        val result = pushService.ensurePusherIsRegistered(
            FakeMatrixClient(
                sessionVerificationService = sessionVerificationService,
            )
        )
        assertThat(result.isFailure).isTrue()
        lambda.assertions()
            .isCalledOnce()
            .with(
                // MatrixClient
                any(),
                // First distributor
                value(pushService.getAvailablePushProviders()[0].getDistributors()[0]),
            )
    }

    @Test
    fun `ensurePusher - if current push provider does not have distributors, nothing happen`() = runTest {
        val lambda = lambdaRecorder<MatrixClient, Distributor, Result<Unit>> { _, _ ->
            Result.success(Unit)
        }
        val sessionVerificationService = FakeSessionVerificationService(
            initialSessionVerifiedStatus = SessionVerifiedStatus.Verified
        )
        val pushProvider = FakePushProvider(
            index = 0,
            name = "aFakePushProvider0",
            distributors = emptyList(),
            registerWithResult = lambda,
        )
        val pushService = createDefaultPushService(
            pushProviders = setOf(pushProvider),
            getCurrentPushProvider = FakeGetCurrentPushProvider(currentPushProvider = pushProvider.name),
        )
        val result = pushService.ensurePusherIsRegistered(
            FakeMatrixClient(
                sessionVerificationService = sessionVerificationService,
            )
        )
        assertThat(result.exceptionOrNull())
            .isInstanceOf(PusherRegistrationFailure.NoDistributorsAvailable::class.java)
        lambda.assertions()
            .isNeverCalled()
    }

    @Test
    fun `ensurePusher - ensure current provider is registered with current distributor`() = runTest {
        val lambda = lambdaRecorder<MatrixClient, Distributor, Result<Unit>> { _, _ ->
            Result.success(Unit)
        }
        val sessionVerificationService = FakeSessionVerificationService(
            initialSessionVerifiedStatus = SessionVerifiedStatus.Verified
        )
        val distributor = Distributor("aDistributorValue1", "aDistributorName1")
        val pushProvider = FakePushProvider(
            index = 0,
            name = "aFakePushProvider0",
            distributors = listOf(
                Distributor("aDistributorValue0", "aDistributorName0"),
                distributor,
            ),
            currentDistributor = { distributor },
            registerWithResult = lambda,
        )
        val pushService = createDefaultPushService(
            pushProviders = setOf(pushProvider),
            getCurrentPushProvider = FakeGetCurrentPushProvider(currentPushProvider = pushProvider.name),
        )
        val result = pushService.ensurePusherIsRegistered(
            FakeMatrixClient(
                sessionVerificationService = sessionVerificationService,
            )
        )
        assertThat(result.isSuccess).isTrue()
        lambda.assertions()
            .isCalledOnce()
            .with(
                // MatrixClient
                any(),
                // Current distributor
                value(distributor),
            )
    }

    @Test
    fun `ensurePusher - case no push provider available provider`() = runTest {
        val lambda = lambdaRecorder<MatrixClient, Distributor, Result<Unit>> { _, _ ->
            Result.success(Unit)
        }
        val sessionVerificationService = FakeSessionVerificationService(SessionVerifiedStatus.Verified)
        val pushService = createDefaultPushService(
            pushProviders = emptySet(),
        )
        val result = pushService.ensurePusherIsRegistered(
            FakeMatrixClient(
                sessionVerificationService = sessionVerificationService,
            )
        )
        assertThat(result.exceptionOrNull())
            .isInstanceOf(PusherRegistrationFailure.NoProvidersAvailable::class.java)
        lambda.assertions()
            .isNeverCalled()
    }

    @Test
    fun `ensurePusher - if current push provider does not have current distributor, the first one is used`() = runTest {
        val lambda = lambdaRecorder<MatrixClient, Distributor, Result<Unit>> { _, _ ->
            Result.success(Unit)
        }
        val sessionVerificationService = FakeSessionVerificationService(
            initialSessionVerifiedStatus = SessionVerifiedStatus.Verified
        )
        val pushProvider = FakePushProvider(
            index = 0,
            name = "aFakePushProvider0",
            distributors = listOf(
                Distributor("aDistributorValue0", "aDistributorName0"),
                Distributor("aDistributorValue1", "aDistributorName1"),
            ),
            currentDistributor = { null },
            registerWithResult = lambda,
        )
        val pushService = createDefaultPushService(
            pushProviders = setOf(pushProvider),
            getCurrentPushProvider = FakeGetCurrentPushProvider(currentPushProvider = pushProvider.name),
        )
        val result = pushService.ensurePusherIsRegistered(
            FakeMatrixClient(
                sessionVerificationService = sessionVerificationService,
            )
        )
        assertThat(result.isSuccess).isTrue()
        lambda.assertions()
            .isCalledOnce()
            .with(
                // MatrixClient
                any(),
                // First distributor
                value(pushService.getAvailablePushProviders()[0].getDistributors()[0]),
            )
    }

    private fun createDefaultPushService(
        testPush: TestPush = FakeTestPush(),
        userPushStoreFactory: UserPushStoreFactory = FakeUserPushStoreFactory(),
        pushProviders: Set<@JvmSuppressWildcards PushProvider> = emptySet(),
        getCurrentPushProvider: GetCurrentPushProvider = FakeGetCurrentPushProvider(currentPushProvider = null),
        sessionObserver: SessionObserver = NoOpSessionObserver(),
        pushClientSecretStore: PushClientSecretStore = InMemoryPushClientSecretStore(),
        pushDataStore: PushDataStore = InMemoryPushDataStore(),
        mutableBatteryOptimizationStore: MutableBatteryOptimizationStore = FakeMutableBatteryOptimizationStore(),
        serviceUnregisteredHandler: ServiceUnregisteredHandler = FakeServiceUnregisteredHandler(),
    ): DefaultPushService {
        return DefaultPushService(
            testPush = testPush,
            userPushStoreFactory = userPushStoreFactory,
            pushProviders = pushProviders,
            getCurrentPushProvider = getCurrentPushProvider,
            sessionObserver = sessionObserver,
            pushClientSecretStore = pushClientSecretStore,
            pushDataStore = pushDataStore,
            mutableBatteryOptimizationStore = mutableBatteryOptimizationStore,
            serviceUnregisteredHandler = serviceUnregisteredHandler,
        )
    }
}
