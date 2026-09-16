/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.api

import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.push.api.history.PushHistoryItem
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import kotlinx.coroutines.flow.Flow

/**
 * Entry point for push notification setup: choosing a push provider, registering a pusher with the homeserver, and inspecting the push history.
 */
interface PushService {
    /**
     * Return the current push provider, or null if none.
     *
     * @param sessionId the session to read the provider of.
     */
    suspend fun getCurrentPushProvider(sessionId: SessionId): PushProvider?

    /**
     * Return the list of push providers, available at compile time, sorted by index.
     */
    fun getAvailablePushProviders(): List<PushProvider>

    /**
     * Will unregister any previous pusher and register a new one with the provided [PushProvider].
     *
     * The method has effect only if the [PushProvider] is different than the current one.
     *
     * @param matrixClient the session to register the pusher for.
     * @param pushProvider the provider to switch to.
     * @param distributor the distributor of that provider to use, which matters for providers such as UnifiedPush.
     */
    suspend fun registerWith(
        matrixClient: MatrixClient,
        pushProvider: PushProvider,
        distributor: Distributor,
    ): Result<Unit>

    /**
     * Ensure that the pusher with the current push provider and distributor is registered.
     * If there is no current config, the default push provider with the default distributor will be used.
     * Error can be [PusherRegistrationFailure].
     *
     * @param matrixClient the session to ensure the pusher of.
     */
    suspend fun ensurePusherIsRegistered(
        matrixClient: MatrixClient,
    ): Result<Unit>

    /**
     * Store the given push provider as the current one, but do not register.
     * To be used when there is no distributor available.
     *
     * @param sessionId the session to store the provider for.
     * @param pushProvider the provider to remember.
     */
    suspend fun selectPushProvider(
        sessionId: SessionId,
        pushProvider: PushProvider,
    )

    /**
     * Whether the user has asked not to be warned about pusher registration failures for this session.
     *
     * @param sessionId the session to read the preference of.
     */
    fun ignoreRegistrationError(sessionId: SessionId): Flow<Boolean>

    /**
     * Remembers whether pusher registration failures should be reported to the user for this session.
     *
     * @param sessionId the session to store the preference for.
     * @param ignore true to stop warning the user about registration failures.
     */
    suspend fun setIgnoreRegistrationError(sessionId: SessionId, ignore: Boolean)

    /**
     * Asks the homeserver to send a test push, so the user can check that the whole chain works.
     * Return false in case of early error.
     *
     * @param sessionId the session to test the push setup of.
     */
    suspend fun testPush(sessionId: SessionId): Boolean

    /**
     * Get a flow of total number of received Push.
     */
    val pushCounter: Flow<Int>

    /**
     * Get a flow of list of [PushHistoryItem].
     */
    fun getPushHistoryItemsFlow(): Flow<List<PushHistoryItem>>

    /**
     * Reset the push history, including the push counter.
     */
    suspend fun resetPushHistory()

    /**
     * Reset the battery optimization state.
     */
    suspend fun resetBatteryOptimizationState()

    /**
     * Notify the user that the service is un-registered.
     *
     * @param userId the user whose push service is no longer registered.
     */
    suspend fun onServiceUnregistered(userId: UserId)
}
