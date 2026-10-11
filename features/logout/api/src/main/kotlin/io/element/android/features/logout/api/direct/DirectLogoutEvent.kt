/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.logout.api.direct

sealed interface DirectLogoutEvent {
    /**
     * @param ignoreSdkError whether to sign out locally even if the homeserver could not be notified.
     * @param skipConfirmation whether to sign out right away instead of asking the user to confirm first.
     */
    data class Logout(
        val ignoreSdkError: Boolean,
        val skipConfirmation: Boolean = false,
    ) : DirectLogoutEvent
    data object CloseDialogs : DirectLogoutEvent
}
