/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.userprofile.api

sealed interface UserProfileEvent {
    data object StartDM : UserProfileEvent
    data object ClearStartDMState : UserProfileEvent
    data class BlockUser(val needsConfirmation: Boolean = false) : UserProfileEvent
    data class UnblockUser(val needsConfirmation: Boolean = false) : UserProfileEvent
    data object ClearBlockUserError : UserProfileEvent
    data object ClearConfirmationDialog : UserProfileEvent
    data object WithdrawVerification : UserProfileEvent
    data class CopyToClipboard(val text: String) : UserProfileEvent
}
