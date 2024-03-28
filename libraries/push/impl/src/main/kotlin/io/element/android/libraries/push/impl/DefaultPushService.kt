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
import io.element.android.libraries.push.impl.notifications.DefaultNotificationDrawerManager
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.libraries.pushstore.api.UserPushStoreFactory
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultPushService @Inject constructor(
    private val defaultNotificationDrawerManager: DefaultNotificationDrawerManager,
    private val pushersManager: PushersManager,
    private val userPushStoreFactory: UserPushStoreFactory,
    private val pushProviders: Set<@JvmSuppressWildcards PushProvider>,
    private val getCurrentPushProvider: GetCurrentPushProvider,
) : PushService {
    override fun notificationStyleChanged() {
        defaultNotificationDrawerManager.notificationStyleChanged()
    }

    override fun getAvailablePushProviders(): List<PushProvider> {
        return pushProviders
            .filter { it.isAvailable() }
            .sortedBy { it.index }
    }

    /**
     * Get current push provider, compare with provided one, then unregister and register if different, and store change.
     */
    override suspend fun registerWith(matrixClient: MatrixClient, pushProvider: PushProvider, distributor: Distributor) {
        val userPushStore = userPushStoreFactory.create(matrixClient.sessionId)
        val currentPushProviderName = userPushStore.getPushProviderName()
        if (currentPushProviderName != pushProvider.name) {
            // Unregister previous one if any
            pushProviders.find { it.name == currentPushProviderName }?.unregister(matrixClient)
        }
        pushProvider.registerWith(matrixClient, distributor)
        // Store new value
        userPushStore.setPushProviderName(pushProvider.name)
    }

    override suspend fun testPush(): Boolean {
        val currentPushProvider = getCurrentPushProvider.getCurrentPushProvider()
        val pushProvider = pushProviders.find { it.name == currentPushProvider } ?: return false
        val config = pushProvider.getCurrentUserPushConfig() ?: return false
        pushersManager.testPush(config)
        return true
    }
}
