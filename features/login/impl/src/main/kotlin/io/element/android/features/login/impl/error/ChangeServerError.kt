/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.error

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import io.element.android.features.login.impl.R
import io.element.android.features.login.impl.changeserver.UnauthorizedAccountProviderException
import io.element.android.libraries.matrix.api.auth.AuthenticationException
import io.element.android.libraries.ui.strings.CommonStrings

sealed class ChangeServerError : Throwable() {
    data class Error(
        @StringRes val messageId: Int? = null,
        val messageStr: String? = null,
    ) : ChangeServerError() {
        @Composable
        @ReadOnlyComposable
        fun message(): String = messageStr ?: stringResource(messageId ?: CommonStrings.error_unknown)
    }

    data class UnauthorizedAccountProvider(
        val unauthorisedAccountProviderTitle: String,
        val authorisedAccountProviderTitles: List<String>,
    ) : ChangeServerError()

    data object SlidingSyncAlert : ChangeServerError()

    companion object {
        fun from(error: Throwable): ChangeServerError = when (error) {
            is AuthenticationException.SlidingSyncVersion -> SlidingSyncAlert
            is AuthenticationException.Oidc -> Error(messageStr = error.message)
            is UnauthorizedAccountProviderException -> UnauthorizedAccountProvider(
                unauthorisedAccountProviderTitle = error.unauthorisedAccountProviderTitle,
                authorisedAccountProviderTitles = error.authorisedAccountProviderTitles,
            )
            else -> Error(messageId = R.string.screen_change_server_error_invalid_homeserver)
        }
    }
}
