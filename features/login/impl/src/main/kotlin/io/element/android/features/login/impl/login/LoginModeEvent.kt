/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.login

import io.element.android.libraries.matrix.api.auth.MatrixHomeServerDetails

sealed interface LoginModeEvent {
    data class Submit(
        val isAccountCreation: Boolean,
        val homeserverUrl: String,
        val resolvedHomeserverUrl: String?,
        val loginHint: String?,
        // When the caller has already configured the homeserver (e.g. the account provider screen validated
        // it first), these are its resolved details. When set, login reuses them instead of calling
        // setHomeserver again, avoiding a redundant network round-trip.
        val preConfiguredDetails: MatrixHomeServerDetails? = null,
    ) : LoginModeEvent

    data object ClearError : LoginModeEvent

    data object RequestLocalNetworkPermission : LoginModeEvent

    data object DismissLocalNetworkPermission : LoginModeEvent
}
