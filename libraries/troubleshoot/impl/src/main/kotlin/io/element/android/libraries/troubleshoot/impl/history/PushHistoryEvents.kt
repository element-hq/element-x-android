/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.troubleshoot.impl.history

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId

sealed interface PushHistoryEvents {
    data class SetShowOnlyErrors(val showOnlyErrors: Boolean) : PushHistoryEvents
    data class Reset(val requiresConfirmation: Boolean) : PushHistoryEvents
    data class NavigateTo(val sessionId: SessionId, val roomId: RoomId, val eventId: EventId) : PushHistoryEvents
    data object ClearDialog : PushHistoryEvents
}
