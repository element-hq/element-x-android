/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.auth

sealed class AuthenticationException(message: String?) : Exception(message) {
    data class AccountAlreadyLoggedIn(
        val userId: String,
    ) : AuthenticationException(null)

    class InvalidServerName(message: String?) : AuthenticationException(message)
    class SlidingSyncVersion(message: String?) : AuthenticationException(message)
    class ServerUnreachable(message: String?) : AuthenticationException(message)
    class Oidc(message: String?) : AuthenticationException(message)
    class Generic(message: String?) : AuthenticationException(message)
}
