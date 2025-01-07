/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.troubleshoot.api.test

data class NotificationTroubleshootTestState(
    val name: String,
    val description: String,
    val status: Status,
) {
    sealed interface Status {
        data class Idle(val visible: Boolean) : Status
        data object InProgress : Status
        data object WaitingForUser : Status
        data object Success : Status
        data class Failure(val hasQuickFix: Boolean) : Status
    }
}
