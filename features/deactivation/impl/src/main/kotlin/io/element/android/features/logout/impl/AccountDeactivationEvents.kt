/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.logout.impl

sealed interface AccountDeactivationEvents {
    data class SetEraseData(val eraseData: Boolean) : AccountDeactivationEvents
    data class SetPassword(val password: String) : AccountDeactivationEvents
    data class DeactivateAccount(val isRetry: Boolean) : AccountDeactivationEvents
    data object CloseDialogs : AccountDeactivationEvents
}
