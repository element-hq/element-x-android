/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.auth

sealed interface OidcPrompt {
    /**
     * The Authorization Server must not display any authentication or consent
     * user interface pages.
     */
    data object None : OidcPrompt

    /**
     * The Authorization Server should prompt the End-User for
     * reauthentication.
     */
    data object Login : OidcPrompt

    /**
     * The Authorization Server should prompt the End-User for consent before
     * returning information to the Client.
     */
    data object Consent : OidcPrompt

    /**
     * The Authorization Server should prompt the End-User to select a user
     * account.
     *
     * This enables an End-User who has multiple accounts at the Authorization
     * Server to select amongst the multiple accounts that they might have
     * current sessions for.
     */
    data object SelectAccount : OidcPrompt

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
