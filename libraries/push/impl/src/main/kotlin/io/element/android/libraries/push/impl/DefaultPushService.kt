/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.api.GetCurrentPushProvider
import io.element.android.libraries.push.api.PushService
import io.element.android.libraries.push.api.history.PushHistoryItem
import io.element.android.libraries.push.impl.store.PushDataStore
import io.element.android.libraries.push.impl.test.TestPush
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.libraries.pushstore.api.UserPushStoreFactory
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecretStore
import io.element.android.libraries.sessionstorage.api.observer.SessionListener
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

@ContributesBinding(AppScope::class, boundType = PushService::class)
@SingleIn(AppScope::class)
class DefaultPushService @Inject constructor(
    private val testPush: TestPush,
    private val userPushStoreFactory: UserPushStoreFactory,
    private val pushProviders: Set<@JvmSuppressWildcards PushProvider>,
    private val getCurrentPushProvider: GetCurrentPushProvider,
    private val sessionObserver: SessionObserver,
    private val pushClientSecretStore: PushClientSecretStore,
    private val pushDataStore: PushDataStore,
) : PushService, SessionListener {
    init {
        observeSessions()
    }

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
        val currentDistributorValue = currentPushProvider?.getCurrentDistributor(matrixClient.sessionId)?.value
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
        sessionId: SessionId,
        pushProvider: PushProvider,
    ) {
        Timber.d("Select ${pushProvider.name}")
        val userPushStore = userPushStoreFactory.getOrCreate(sessionId)
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

    private fun observeSessions() {
        sessionObserver.addListener(this)
    }

    override suspend fun onSessionCreated(userId: String) {
        // Nothing to do
    }

    /**
     * The session has been deleted.
     * In this case, this is not necessary to unregister the pusher from the homeserver,
     * but we need to do some cleanup locally.
     * The current push provider may want to take action, and we need to
     * cleanup the stores.
     */
    override suspend fun onSessionDeleted(userId: String) {
        val sessionId = SessionId(userId)
        val userPushStore = userPushStoreFactory.getOrCreate(sessionId)
        val currentPushProviderName = userPushStore.getPushProviderName()
        val currentPushProvider = pushProviders.find { it.name == currentPushProviderName }
        // Cleanup the current push provider. They may need the client secret, so delete the secret after.
        currentPushProvider?.onSessionDeleted(sessionId)
        // Now we can safely reset the stores.
        pushClientSecretStore.resetSecret(sessionId)
        userPushStore.reset()
    }

    override val pushCounter: Flow<Int> = pushDataStore.pushCounterFlow

    override fun getPushHistoryItemsFlow(): Flow<List<PushHistoryItem>> {
        return pushDataStore.getPushHistoryItemsFlow()
    }

    override suspend fun resetPushHistory() {
        pushDataStore.reset()
    }
}
