/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.api

import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import kotlinx.coroutines.flow.Flow

interface PushService {
    /**
     * Return the current push provider, or null if none.
     */
    suspend fun getCurrentPushProvider(): PushProvider?

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
    suspend fun testPush(): Boolean
}
