/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.api

import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.SessionId

/**
 * This is the main API for this module.
 */
interface PushProvider {
    /**
     * Allow to sort providers, from lower index to higher index.
     */
    val index: Int

    /**
     * User friendly name.
     */
    val name: String

    /**
     * true if the Push provider supports multiple distributors.
     */
    val supportMultipleDistributors: Boolean

    /**
     * Return the list of available distributors.
     */
    fun getDistributors(): List<Distributor>

    /**
     * Register the pusher to the homeserver.
     *
     * @param matrixClient the session to register the pusher for.
     * @param distributor the distributor to route the notifications through.
     */
    suspend fun registerWith(matrixClient: MatrixClient, distributor: Distributor): Result<Unit>

    /**
     * Return the current distributor as its raw stored value, or null if none.
     *
     * @param sessionId the session to read the distributor of.
     */
    suspend fun getCurrentDistributorValue(sessionId: SessionId): String?

    /**
     * Return the current distributor, or null if none, which also happens when the stored one is no longer installed.
     *
     * @param sessionId the session to read the distributor of.
     */
    suspend fun getCurrentDistributor(sessionId: SessionId): Distributor?

    /**
     * Unregister the pusher.
     *
     * @param matrixClient the session to unregister the pusher of.
     */
    suspend fun unregister(matrixClient: MatrixClient): Result<Unit>

    /**
     * To invoke when the session is deleted.
     *
     * @param sessionId the session that has been removed, whose provider-side data should be cleaned up.
     */
    suspend fun onSessionDeleted(sessionId: SessionId)

    /**
     * The gateway and push key currently in use, for display in the troubleshooting screens; `null` when not registered.
     *
     * @param sessionId the session to read the configuration of.
     */
    suspend fun getPushConfig(sessionId: SessionId): Config?

    /** Whether this provider can renew its push token on demand, which not every provider supports. */
    fun canRotateToken(): Boolean

    /**
     * Renews the push token, which is one of the recovery steps offered when notifications stop arriving.
     * Throws when [canRotateToken] is `false`.
     */
    suspend fun rotateToken(): Result<Unit> {
        error("rotateToken() not implemented, you need to override this method in your implementation")
    }
}
