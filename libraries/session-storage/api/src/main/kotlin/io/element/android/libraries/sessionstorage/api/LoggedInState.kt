/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.sessionstorage.api

sealed interface LoggedInState {
    data object NotLoggedIn : LoggedInState
    data class LoggedIn(
        val sessionId: String,
        val isTokenValid: Boolean,
    ) : LoggedInState
}
