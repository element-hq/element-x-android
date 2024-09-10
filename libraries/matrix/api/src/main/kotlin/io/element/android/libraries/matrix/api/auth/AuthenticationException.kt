/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.auth

sealed class AuthenticationException(message: String) : Exception(message) {
    class InvalidServerName(message: String) : AuthenticationException(message)
    class SlidingSyncVersion(message: String) : AuthenticationException(message)
    class Generic(message: String) : AuthenticationException(message)
}
