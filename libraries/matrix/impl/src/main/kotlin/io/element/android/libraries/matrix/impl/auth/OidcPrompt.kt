/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.auth

import io.element.android.libraries.matrix.api.auth.OidcPrompt
import org.matrix.rustcomponents.sdk.OidcPrompt as RustOidcPrompt

internal fun OidcPrompt.toRustPrompt(): RustOidcPrompt {
    return when (this) {
        OidcPrompt.None -> RustOidcPrompt.None
        OidcPrompt.Login -> RustOidcPrompt.Login
        OidcPrompt.Consent -> RustOidcPrompt.Consent
        OidcPrompt.SelectAccount -> RustOidcPrompt.SelectAccount
        OidcPrompt.Create -> RustOidcPrompt.Create
        is OidcPrompt.Unknown -> RustOidcPrompt.Unknown(value)
    }
}
