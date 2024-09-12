/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.appnav.loggedin

import io.element.android.libraries.matrix.api.exception.ClientException

sealed class PusherRegistrationFailure : Exception() {
    class AccountNotVerified : PusherRegistrationFailure()
    class NoProvidersAvailable : PusherRegistrationFailure()
    class NoDistributorsAvailable : PusherRegistrationFailure()

    /**
     * @param clientException the failure that occurred.
     * @param isRegisteringAgain true if the server should already have a the same pusher registered.
     */
    class RegistrationFailure(
        val clientException: ClientException,
        val isRegisteringAgain: Boolean,
    ) : PusherRegistrationFailure()
}
