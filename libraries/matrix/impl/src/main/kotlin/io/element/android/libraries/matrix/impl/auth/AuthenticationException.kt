/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.auth

import io.element.android.libraries.matrix.api.auth.AuthenticationException
import org.matrix.rustcomponents.sdk.ClientBuildException

fun Throwable.mapAuthenticationException(): AuthenticationException {
    val message = this.message ?: "Unknown error"
    return when (this) {
        is ClientBuildException -> when (this) {
            is ClientBuildException.Generic -> AuthenticationException.Generic(message)
            is ClientBuildException.InvalidServerName -> AuthenticationException.InvalidServerName(message)
            is ClientBuildException.SlidingSyncVersion -> AuthenticationException.SlidingSyncVersion(message)
            is ClientBuildException.Sdk -> AuthenticationException.Generic(message)
            is ClientBuildException.ServerUnreachable -> AuthenticationException.Generic(message)
            is ClientBuildException.SlidingSync -> AuthenticationException.Generic(message)
            is ClientBuildException.WellKnownDeserializationException -> AuthenticationException.Generic(message)
            is ClientBuildException.WellKnownLookupFailed -> AuthenticationException.Generic(message)
        }
        else -> AuthenticationException.Generic(message)
    }
}
