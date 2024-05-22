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

package io.element.android.libraries.push.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.push.api.GetCurrentPushProvider
import io.element.android.libraries.push.impl.test.FakeTestPush
import io.element.android.libraries.push.impl.test.TestPush
import io.element.android.libraries.push.test.FakeGetCurrentPushProvider
import io.element.android.libraries.pushproviders.api.CurrentUserPushConfig
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.libraries.pushproviders.test.FakePushProvider
import io.element.android.libraries.pushstore.api.UserPushStoreFactory
import io.element.android.libraries.pushstore.test.userpushstore.FakeUserPushStore
import io.element.android.libraries.pushstore.test.userpushstore.FakeUserPushStoreFactory
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
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
        val aConfig = CurrentUserPushConfig(
            url = "aUrl",
            pushKey = "aPushKey",
        )
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

    private fun createDefaultPushService(
        testPush: TestPush = FakeTestPush(),
        userPushStoreFactory: UserPushStoreFactory = FakeUserPushStoreFactory(),
        pushProviders: Set<@JvmSuppressWildcards PushProvider> = emptySet(),
        getCurrentPushProvider: GetCurrentPushProvider = FakeGetCurrentPushProvider(currentPushProvider = null),
    ): DefaultPushService {
        return DefaultPushService(
            testPush = testPush,
            userPushStoreFactory = userPushStoreFactory,
            pushProviders = pushProviders,
            getCurrentPushProvider = getCurrentPushProvider,
        )
    }
}
