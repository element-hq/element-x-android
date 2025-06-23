/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.push.api.GetCurrentPushProvider
import io.element.android.libraries.push.impl.store.InMemoryPushDataStore
import io.element.android.libraries.push.impl.store.PushDataStore
import io.element.android.libraries.push.impl.test.FakeTestPush
import io.element.android.libraries.push.impl.test.TestPush
import io.element.android.libraries.push.test.FakeGetCurrentPushProvider
import io.element.android.libraries.pushproviders.api.CurrentUserPushConfig
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.libraries.pushproviders.test.FakePushProvider
import io.element.android.libraries.pushproviders.test.aCurrentUserPushConfig
import io.element.android.libraries.pushstore.api.UserPushStoreFactory
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecretStore
import io.element.android.libraries.pushstore.test.userpushstore.FakeUserPushStore
import io.element.android.libraries.pushstore.test.userpushstore.FakeUserPushStoreFactory
import io.element.android.libraries.pushstore.test.userpushstore.clientsecret.InMemoryPushClientSecretStore
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver
import io.element.android.libraries.sessionstorage.test.observer.NoOpSessionObserver
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultPushServiceTest {
    @Test
    fun `test push no push provider`() = runTest {
        val defaultPushService = createDefaultPushService()
        assertThat(defaultPushService.testPush()).isFalse()
    }

    @Test
    fun `test push no config`() = runTest {
        val aPushProvider = FakePushProvider()
        val defaultPushService = createDefaultPushService(
            pushProviders = setOf(aPushProvider),
            getCurrentPushProvider = FakeGetCurrentPushProvider(currentPushProvider = aPushProvider.name),
        )
        assertThat(defaultPushService.testPush()).isFalse()
    }

    @Test
    fun `test push ok`() = runTest {
        val aConfig = aCurrentUserPushConfig()
        val testPushResult = lambdaRecorder<CurrentUserPushConfig, Unit> { }
        val aPushProvider = FakePushProvider(
            currentUserPushConfig = aConfig
        )
        val defaultPushService = createDefaultPushService(
            pushProviders = setOf(aPushProvider),
            getCurrentPushProvider = FakeGetCurrentPushProvider(currentPushProvider = aPushProvider.name),
            testPush = FakeTestPush(executeResult = testPushResult),
        )
        assertThat(defaultPushService.testPush()).isTrue()
        testPushResult.assertions()
            .isCalledOnce()
            .with(value(aConfig))
    }

    @Test
    fun `getCurrentPushProvider null`() = runTest {
        val defaultPushService = createDefaultPushService()
        val result = defaultPushService.getCurrentPushProvider()
        assertThat(result).isNull()
    }

    @Test
    fun `getCurrentPushProvider ok`() = runTest {
        val aPushProvider = FakePushProvider()
        val defaultPushService = createDefaultPushService(
            pushProviders = setOf(aPushProvider),
            getCurrentPushProvider = FakeGetCurrentPushProvider(currentPushProvider = aPushProvider.name),
        )
        val result = defaultPushService.getCurrentPushProvider()
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
        defaultPushService.onSessionDeleted(A_SESSION_ID.value)
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
        defaultPushService.onSessionDeleted(A_SESSION_ID.value)
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

    private fun createDefaultPushService(
        testPush: TestPush = FakeTestPush(),
        userPushStoreFactory: UserPushStoreFactory = FakeUserPushStoreFactory(),
        pushProviders: Set<@JvmSuppressWildcards PushProvider> = emptySet(),
        getCurrentPushProvider: GetCurrentPushProvider = FakeGetCurrentPushProvider(currentPushProvider = null),
        sessionObserver: SessionObserver = NoOpSessionObserver(),
        pushClientSecretStore: PushClientSecretStore = InMemoryPushClientSecretStore(),
        pushDataStore: PushDataStore = InMemoryPushDataStore(),
    ): DefaultPushService {
        return DefaultPushService(
            testPush = testPush,
            userPushStoreFactory = userPushStoreFactory,
            pushProviders = pushProviders,
            getCurrentPushProvider = getCurrentPushProvider,
            sessionObserver = sessionObserver,
            pushClientSecretStore = pushClientSecretStore,
            pushDataStore = pushDataStore,
        )
    }
}
