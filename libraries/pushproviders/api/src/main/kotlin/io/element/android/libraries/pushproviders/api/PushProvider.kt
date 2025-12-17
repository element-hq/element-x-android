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
     */
    suspend fun registerWith(matrixClient: MatrixClient, distributor: Distributor): Result<Unit>

    /**
     * Return the current distributor, or null if none.
     */
    suspend fun getCurrentDistributorValue(sessionId: SessionId): String?

    /**
     * Return the current distributor, or null if none.
     */
    suspend fun getCurrentDistributor(sessionId: SessionId): Distributor?

    /**
     * Unregister the pusher.
     */
    suspend fun unregister(matrixClient: MatrixClient): Result<Unit>

    /**
     * To invoke when the session is deleted.
     */
    suspend fun onSessionDeleted(sessionId: SessionId)

    suspend fun getPushConfig(sessionId: SessionId): Config?

    fun canRotateToken(): Boolean

    suspend fun rotateToken(): Result<Unit> {
        error("rotateToken() not implemented, you need to override this method in your implementation")
    }
}
