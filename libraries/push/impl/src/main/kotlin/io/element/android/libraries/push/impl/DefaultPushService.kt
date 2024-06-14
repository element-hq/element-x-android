/*
 * Copyright (c) 2023 New Vector Ltd
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

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.push.api.GetCurrentPushProvider
import io.element.android.libraries.push.api.PushService
import io.element.android.libraries.push.impl.test.TestPush
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.libraries.pushstore.api.UserPushStoreFactory
import timber.log.Timber
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultPushService @Inject constructor(
    private val testPush: TestPush,
    private val userPushStoreFactory: UserPushStoreFactory,
    private val pushProviders: Set<@JvmSuppressWildcards PushProvider>,
    private val getCurrentPushProvider: GetCurrentPushProvider,
) : PushService {
    override suspend fun getCurrentPushProvider(): PushProvider? {
        val currentPushProvider = getCurrentPushProvider.getCurrentPushProvider()
        return pushProviders.find { it.name == currentPushProvider }
    }

    override fun getAvailablePushProviders(): List<PushProvider> {
        return pushProviders
            .filter { it.isAvailable() }
            .sortedBy { it.index }
    }

    override suspend fun registerWith(
        matrixClient: MatrixClient,
        pushProvider: PushProvider,
        distributor: Distributor,
    ): Result<Unit> {
        Timber.d("Registering with ${pushProvider.name}/${distributor.name}")
        val userPushStore = userPushStoreFactory.getOrCreate(matrixClient.sessionId)
        val currentPushProviderName = userPushStore.getPushProviderName()
        val currentPushProvider = pushProviders.find { it.name == currentPushProviderName }
        val currentDistributorValue = currentPushProvider?.getCurrentDistributor(matrixClient)?.value
        if (currentPushProviderName != pushProvider.name || currentDistributorValue != distributor.value) {
            // Unregister previous one if any
            currentPushProvider
                ?.also { Timber.d("Unregistering previous push provider $currentPushProviderName/$currentDistributorValue") }
                ?.unregister(matrixClient)
                ?.onFailure {
                    Timber.w(it, "Failed to unregister previous push provider")
                    return Result.failure(it)
                }
        }
        // Store new value
        userPushStore.setPushProviderName(pushProvider.name)
        // Then try to register
        return pushProvider.registerWith(matrixClient, distributor)
    }

    override suspend fun testPush(): Boolean {
        val pushProvider = getCurrentPushProvider() ?: return false
        val config = pushProvider.getCurrentUserPushConfig() ?: return false
        testPush.execute(config)
        return true
    }
}
