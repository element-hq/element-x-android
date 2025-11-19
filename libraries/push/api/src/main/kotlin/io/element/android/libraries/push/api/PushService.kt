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

interface PushService {
    /**
     * Return the current push provider, or null if none.
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
     */
    suspend fun ensurePusherIsRegistered(
        matrixClient: MatrixClient,
    ): Result<Unit>

    /**
     * Store the given push provider as the current one, but do not register.
     * To be used when there is no distributor available.
     */
    suspend fun selectPushProvider(
        sessionId: SessionId,
        pushProvider: PushProvider,
    )

    fun ignoreRegistrationError(sessionId: SessionId): Flow<Boolean>
    suspend fun setIgnoreRegistrationError(sessionId: SessionId, ignore: Boolean)

    /**
     * Return false in case of early error.
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
     */
    suspend fun onServiceUnregistered(userId: UserId)
}
