/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.auth

import io.element.android.libraries.matrix.api.auth.AuthenticationException
import org.matrix.rustcomponents.sdk.ClientBuildException
import org.matrix.rustcomponents.sdk.OidcException

fun Throwable.mapAuthenticationException(): AuthenticationException {
    return when (this) {
        is AuthenticationException -> this
        is ClientBuildException -> when (this) {
            is ClientBuildException.Generic -> AuthenticationException.Generic(message)
            is ClientBuildException.InvalidServerName -> AuthenticationException.InvalidServerName(message)
            is ClientBuildException.SlidingSyncVersion -> AuthenticationException.SlidingSyncVersion(message)
            is ClientBuildException.Sdk -> AuthenticationException.Generic(message)
            is ClientBuildException.ServerUnreachable -> AuthenticationException.ServerUnreachable(message)
            is ClientBuildException.SlidingSync -> AuthenticationException.Generic(message)
            is ClientBuildException.WellKnownDeserializationException -> AuthenticationException.Generic(message)
            is ClientBuildException.WellKnownLookupFailed -> AuthenticationException.Generic(message)
            is ClientBuildException.EventCache -> AuthenticationException.Generic(message)
        }
        is OidcException -> when (this) {
            is OidcException.Generic -> AuthenticationException.Oidc(message)
            is OidcException.CallbackUrlInvalid -> AuthenticationException.Oidc(message)
            is OidcException.Cancelled -> AuthenticationException.Oidc(message)
            is OidcException.MetadataInvalid -> AuthenticationException.Oidc(message)
            is OidcException.NotSupported -> AuthenticationException.Oidc(message)
        }
        else -> AuthenticationException.Generic(message)
    }
}
