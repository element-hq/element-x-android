/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.login

import io.element.android.libraries.matrix.api.auth.OidcDetails

/**
 * Represents the different authentication modes available.
 */
sealed interface AuthenticationMode {
    /**
     * Password-based login mode. Added for backwards compatibility.
     */
    data object PasswordLogin : AuthenticationMode

    /**
     * Account creation mode, using a non-OIDC web flow. It's the registration counterpart to [PasswordLogin].
     */
    data class AccountCreation(val url: String) : AuthenticationMode

    /**
     * OIDC-based authentication mode.
     * @param oidcDetails Details required for OIDC authentication.
     * @param isAccountCreation Whether this mode is for account creation or login.
     */
    data class Oidc(val oidcDetails: OidcDetails, val isAccountCreation: Boolean) : AuthenticationMode
}
