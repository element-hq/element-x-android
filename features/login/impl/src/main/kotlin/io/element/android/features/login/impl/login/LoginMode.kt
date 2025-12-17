/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.login

import io.element.android.libraries.matrix.api.auth.OidcDetails

sealed interface LoginMode {
    data object PasswordLogin : LoginMode
    data class Oidc(val oidcDetails: OidcDetails) : LoginMode
    data class AccountCreation(val url: String) : LoginMode
}
