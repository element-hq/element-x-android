/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.login

import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.auth.OAuthDetails
import io.element.android.libraries.permissions.api.localnetwork.LocalNetworkPermissionDialog

data class LoginModeState(
    val loginMode: AsyncData<LoginMode>,
    val localNetworkPermissionDialog: LocalNetworkPermissionDialog,
    val eventSink: (LoginModeEvent) -> Unit,
)

sealed interface LoginMode {
    data object PasswordLogin : LoginMode
    data class OAuth(val oAuthDetails: OAuthDetails) : LoginMode
    data class AccountCreation(val url: String) : LoginMode
}
