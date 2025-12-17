/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.auth

sealed interface OidcPrompt {
    /**
     * The Authorization Server should prompt the End-User for
     * reauthentication.
     */
    data object Login : OidcPrompt

    /**
     * The Authorization Server should prompt the End-User to create a user
     * account.
     *
     * Defined in [Initiating User Registration via OpenID Connect](https://openid.net/specs/openid-connect-prompt-create-1_0.html).
     */
    data object Create : OidcPrompt

    /**
     * An unknown value.
     */
    data class Unknown(val value: String) : OidcPrompt
}
