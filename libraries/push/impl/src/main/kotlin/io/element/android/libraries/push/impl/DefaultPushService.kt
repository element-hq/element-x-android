/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.push.api.GetCurrentPushProvider
import io.element.android.libraries.push.api.PushService
import io.element.android.libraries.push.api.PusherRegistrationFailure
import io.element.android.libraries.push.api.history.PushHistoryItem
import io.element.android.libraries.push.impl.push.MutableBatteryOptimizationStore
import io.element.android.libraries.push.impl.store.PushDataStore
import io.element.android.libraries.push.impl.test.TestPush
import io.element.android.libraries.push.impl.unregistration.ServiceUnregisteredHandler
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.libraries.pushproviders.api.RegistrationFailure
import io.element.android.libraries.pushstore.api.UserPushStoreFactory
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecretStore
import io.element.android.libraries.sessionstorage.api.observer.SessionListener
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import timber.log.Timber

@ContributesBinding(AppScope::class, binding = binding<PushService>())
@SingleIn(AppScope::class)
class DefaultPushService(
    private val testPush: TestPush,
    private val userPushStoreFactory: UserPushStoreFactory,
    private val pushProviders: Set<@JvmSuppressWildcards PushProvider>,
    private val getCurrentPushProvider: GetCurrentPushProvider,
    private val sessionObserver: SessionObserver,
    private val pushClientSecretStore: PushClientSecretStore,
    private val pushDataStore: PushDataStore,
    private val mutableBatteryOptimizationStore: MutableBatteryOptimizationStore,
    private val serviceUnregisteredHandler: ServiceUnregisteredHandler,
) : PushService, SessionListener {
    init {
        observeSessions()
    }

    override suspend fun getCurrentPushProvider(sessionId: SessionId): PushProvider? {
        val currentPushProvider = getCurrentPushProvider.getCurrentPushProvider(sessionId)
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

    override suspend fun ensurePusherIsRegistered(matrixClient: MatrixClient): Result<Unit> {
        val verificationStatus = matrixClient.sessionVerificationService.sessionVerifiedStatus.first()
        if (verificationStatus != SessionVerifiedStatus.Verified) {
            return Result.failure<Unit>(PusherRegistrationFailure.AccountNotVerified())
                .also { Timber.w("Account is not verified") }
        }
        Timber.d("Ensure pusher is registered")
        val currentPushProvider = getCurrentPushProvider(matrixClient.sessionId)
        val result = if (currentPushProvider == null) {
            Timber.d("Register with the first available push provider with at least one distributor")
            val pushProvider = getAvailablePushProviders()
                .firstOrNull { it.getDistributors().isNotEmpty() }
            // Else fallback to the first available push provider (the list should never be empty)
                ?: getAvailablePushProviders().firstOrNull()
                ?: return Result.failure<Unit>(PusherRegistrationFailure.NoProvidersAvailable())
                    .also { Timber.w("No push providers available") }
            val distributor = pushProvider.getDistributors().firstOrNull()
                ?: return Result.failure<Unit>(PusherRegistrationFailure.NoDistributorsAvailable())
                    .also { Timber.w("No distributors available") }
                    .also {
                        // In this case, consider the push provider is chosen.
                        selectPushProvider(matrixClient.sessionId, pushProvider)
                    }
            registerWith(matrixClient, pushProvider, distributor)
        } else {
            val currentPushDistributor = currentPushProvider.getCurrentDistributor(matrixClient.sessionId)
            if (currentPushDistributor == null) {
                Timber.d("Register with the first available distributor")
                val distributor = currentPushProvider.getDistributors().firstOrNull()
                    ?: return Result.failure<Unit>(PusherRegistrationFailure.NoDistributorsAvailable())
                        .also { Timber.w("No distributors available") }
                registerWith(matrixClient, currentPushProvider, distributor)
            } else {
                Timber.d("Re-register with the current distributor")
                registerWith(matrixClient, currentPushProvider, currentPushDistributor)
            }
        }
        return result.fold(
            onSuccess = {
                Timber.d("Pusher registered")
                Result.success(Unit)
            },
            onFailure = {
                Timber.e(it, "Failed to register pusher")
                if (it is RegistrationFailure) {
                    Result.failure(PusherRegistrationFailure.RegistrationFailure(it.clientException, it.isRegisteringAgain))
                } else {
                    Result.failure(it)
                }
            }
        )
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

    override suspend fun testPush(sessionId: SessionId): Boolean {
        val pushProvider = getCurrentPushProvider(sessionId) ?: return false
        val config = pushProvider.getPushConfig(sessionId) ?: return false
        testPush.execute(config)
        return true
    }

    private fun observeSessions() {
        sessionObserver.addListener(this)
    }

    /**
     * The session has been deleted.
     * In this case, this is not necessary to unregister the pusher from the homeserver,
     * but we need to do some cleanup locally.
     * The current push provider may want to take action, and we need to
     * cleanup the stores.
     */
    override suspend fun onSessionDeleted(userId: String, wasLastSession: Boolean) {
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

    override suspend fun resetBatteryOptimizationState() {
        mutableBatteryOptimizationStore.reset()
    }

    override suspend fun onServiceUnregistered(userId: UserId) {
        serviceUnregisteredHandler.handle(userId)
    }
}
