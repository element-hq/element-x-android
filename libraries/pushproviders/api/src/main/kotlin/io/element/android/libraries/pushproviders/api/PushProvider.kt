/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.api

import io.element.android.libraries.matrix.api.MatrixClient

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
    suspend fun getCurrentDistributor(matrixClient: MatrixClient): Distributor?

    /**
     * Unregister the pusher.
     */
    suspend fun unregister(matrixClient: MatrixClient): Result<Unit>

    suspend fun getCurrentUserPushConfig(): CurrentUserPushConfig?
}
