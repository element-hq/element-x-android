/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.oidc.impl.webview

import io.element.android.libraries.oidc.api.OidcAction

sealed interface OidcEvents {
    data object Cancel : OidcEvents
    data class OidcActionEvent(val oidcAction: OidcAction) : OidcEvents
    data object ClearError : OidcEvents
}
