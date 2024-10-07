/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.api.GetCurrentPushProvider
import io.element.android.libraries.push.api.PushService
import io.element.android.libraries.push.impl.test.TestPush
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.libraries.pushstore.api.UserPushStoreFactory
import kotlinx.coroutines.flow.Flow
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

    override suspend fun selectPushProvider(
        matrixClient: MatrixClient,
        pushProvider: PushProvider,
    ) {
        Timber.d("Select ${pushProvider.name}")
        val userPushStore = userPushStoreFactory.getOrCreate(matrixClient.sessionId)
        userPushStore.setPushProviderName(pushProvider.name)
    }

    override fun ignoreRegistrationError(sessionId: SessionId): Flow<Boolean> {
        return userPushStoreFactory.getOrCreate(sessionId).ignoreRegistrationError()
    }

    override suspend fun setIgnoreRegistrationError(sessionId: SessionId, ignore: Boolean) {
        userPushStoreFactory.getOrCreate(sessionId).setIgnoreRegistrationError(ignore)
    }

    override suspend fun testPush(): Boolean {
        val pushProvider = getCurrentPushProvider() ?: return false
        val config = pushProvider.getCurrentUserPushConfig() ?: return false
        testPush.execute(config)
        return true
    }
}
