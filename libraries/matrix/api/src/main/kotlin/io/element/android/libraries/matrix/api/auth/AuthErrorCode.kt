/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.auth

enum class AuthErrorCode(val value: String) {
    UNKNOWN("M_UNKNOWN"),
    USER_DEACTIVATED("M_USER_DEACTIVATED"),
    FORBIDDEN("M_FORBIDDEN")
}

// This is taken from the iOS version. It seems like currently there's no better way to extract error codes
val AuthenticationException.errorCode: AuthErrorCode
    get() {
        val message = (this as? AuthenticationException.Generic)?.message ?: return AuthErrorCode.UNKNOWN
        return enumValues<AuthErrorCode>()
            .firstOrNull { message.contains(it.value) }
            ?: AuthErrorCode.UNKNOWN
    }
