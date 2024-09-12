/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.api

import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.exception.ClientException

interface PusherSubscriber {
    /**
     * Register a pusher. Note that failure will be a [RegistrationFailure].
     */
    suspend fun registerPusher(matrixClient: MatrixClient, pushKey: String, gateway: String): Result<Unit>

    /**
     * Unregister a pusher.
     */
    suspend fun unregisterPusher(matrixClient: MatrixClient, pushKey: String, gateway: String): Result<Unit>
}

class RegistrationFailure(
    val clientException: ClientException,
    val isRegisteringAgain: Boolean
) : Exception(clientException)
