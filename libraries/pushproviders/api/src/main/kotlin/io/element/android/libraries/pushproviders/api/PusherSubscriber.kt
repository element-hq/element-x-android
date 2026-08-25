/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.api

import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.exception.ClientException

/**
 * Registers and removes a pusher on the homeserver on behalf of a push provider, so every provider agrees on the parameters sent.
 */
interface PusherSubscriber {
    /**
     * Register a pusher. Note that failure will be a [RegistrationFailure].
     *
     * @param matrixClient the session to register the pusher for.
     * @param pushKey the token identifying this device with the gateway.
     * @param gateway the URL the homeserver should send the notifications to.
     */
    suspend fun registerPusher(matrixClient: MatrixClient, pushKey: String, gateway: String): Result<Unit>

    /**
     * Unregister a pusher.
     *
     * @param matrixClient the session to unregister the pusher of.
     * @param pushKey the token of the registration to remove.
     * @param gateway the gateway of the registration to remove.
     */
    suspend fun unregisterPusher(matrixClient: MatrixClient, pushKey: String, gateway: String): Result<Unit>
}

class RegistrationFailure(
    val clientException: ClientException,
    val isRegisteringAgain: Boolean
) : Exception(clientException)
