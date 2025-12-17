/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.auth

import io.element.android.libraries.matrix.api.auth.OidcPrompt
import org.matrix.rustcomponents.sdk.OidcPrompt as RustOidcPrompt

internal fun OidcPrompt.toRustPrompt(): RustOidcPrompt {
    return when (this) {
        OidcPrompt.Login -> RustOidcPrompt.Unknown("consent")
        OidcPrompt.Create -> RustOidcPrompt.Create
        is OidcPrompt.Unknown -> RustOidcPrompt.Unknown(value)
    }
}
