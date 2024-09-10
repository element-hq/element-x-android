/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.auth

import io.element.android.libraries.matrix.api.auth.AuthenticationException
import org.matrix.rustcomponents.sdk.ClientBuildException as RustAuthenticationException

fun Throwable.mapAuthenticationException(): AuthenticationException {
    val message = this.message ?: "Unknown error"
    return when (this) {
        is RustAuthenticationException.Generic -> AuthenticationException.Generic(message)
        is RustAuthenticationException.InvalidServerName -> AuthenticationException.InvalidServerName(message)
        is RustAuthenticationException.SlidingSyncVersion -> AuthenticationException.SlidingSyncVersion(message)
        else -> AuthenticationException.Generic(message)
    }
}
