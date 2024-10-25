/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.userprofile.api

sealed interface UserProfileEvents {
    data object StartDM : UserProfileEvents
    data object ClearStartDMState : UserProfileEvents
    data class BlockUser(val needsConfirmation: Boolean = false) : UserProfileEvents
    data class UnblockUser(val needsConfirmation: Boolean = false) : UserProfileEvents
    data object ClearBlockUserError : UserProfileEvents
    data object ClearConfirmationDialog : UserProfileEvents
}
