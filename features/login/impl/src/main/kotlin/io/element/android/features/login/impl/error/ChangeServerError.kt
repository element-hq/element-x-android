/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.error

import io.element.android.features.login.impl.changeserver.AccountProviderAccessException
import io.element.android.libraries.matrix.api.auth.AuthenticationException

sealed class ChangeServerError : Exception() {
    data class Error(
        val messageStr: String? = null,
    ) : ChangeServerError()

    data class NeedElementPro(
        val unauthorisedAccountProviderTitle: String,
        val applicationId: String,
    ) : ChangeServerError()

    data class UnauthorizedAccountProvider(
        val unauthorisedAccountProviderTitle: String,
        val authorisedAccountProviderTitles: List<String>,
    ) : ChangeServerError()

    data object SlidingSyncAlert : ChangeServerError()
    data object InvalidServer : ChangeServerError()
    data object UnsupportedServer : ChangeServerError()

    companion object {
        fun from(error: Throwable): ChangeServerError = when (error) {
            is ChangeServerError -> error
            is AuthenticationException -> {
                when (error) {
                    is AuthenticationException.SlidingSyncVersion -> SlidingSyncAlert
                    is AuthenticationException.InvalidServerName,
                    is AuthenticationException.ServerUnreachable -> InvalidServer
                    // AccountAlreadyLoggedIn error should not happen at this point
                    is AuthenticationException.AccountAlreadyLoggedIn -> Error(messageStr = error.message)
                    is AuthenticationException.Generic -> Error(messageStr = error.message)
                    is AuthenticationException.Oidc -> Error(messageStr = error.message)
                }
            }
            is AccountProviderAccessException.NeedElementProException -> NeedElementPro(
                unauthorisedAccountProviderTitle = error.unauthorisedAccountProviderTitle,
                applicationId = error.applicationId,
            )
            is AccountProviderAccessException.UnauthorizedAccountProviderException -> UnauthorizedAccountProvider(
                unauthorisedAccountProviderTitle = error.unauthorisedAccountProviderTitle,
                authorisedAccountProviderTitles = error.authorisedAccountProviderTitles,
            )
            else -> Error(messageStr = error.message)
        }
    }
}
