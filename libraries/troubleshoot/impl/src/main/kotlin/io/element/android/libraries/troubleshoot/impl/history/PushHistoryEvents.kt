/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.troubleshoot.impl.history

sealed interface PushHistoryEvents {
    data class SetShowOnlyErrors(val showOnlyErrors: Boolean) : PushHistoryEvents
    data class Reset(val requiresConfirmation: Boolean) : PushHistoryEvents
    data object ClearDialog : PushHistoryEvents
}
