/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.login

sealed interface LoginModeEvent {
    data class Submit(
        val isAccountCreation: Boolean,
        val homeserverUrl: String,
        val resolvedHomeserverUrl: String?,
        val loginHint: String?,
    ) : LoginModeEvent

    data object ClearError : LoginModeEvent

    data object RequestLocalNetworkPermission : LoginModeEvent

    data object DismissLocalNetworkPermission : LoginModeEvent
}
