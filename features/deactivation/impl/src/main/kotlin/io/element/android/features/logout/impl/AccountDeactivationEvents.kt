/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.logout.impl

sealed interface AccountDeactivationEvents {
    data class SetEraseData(val eraseData: Boolean) : AccountDeactivationEvents
    data class SetPassword(val password: String) : AccountDeactivationEvents
    data class DeactivateAccount(val isRetry: Boolean) : AccountDeactivationEvents
    data object CloseDialogs : AccountDeactivationEvents
}
