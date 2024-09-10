/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.error

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.element.android.features.login.impl.R
import io.element.android.libraries.matrix.api.auth.AuthenticationException

sealed class ChangeServerError : Throwable() {
    data class Error(@StringRes val messageId: Int) : ChangeServerError() {
        @Composable
        fun message(): String = stringResource(messageId)
    }
    data object SlidingSyncAlert : ChangeServerError()

    companion object {
        fun from(error: Throwable): ChangeServerError = when (error) {
            is AuthenticationException.SlidingSyncVersion -> SlidingSyncAlert
            else -> Error(R.string.screen_change_server_error_invalid_homeserver)
        }
    }
}
