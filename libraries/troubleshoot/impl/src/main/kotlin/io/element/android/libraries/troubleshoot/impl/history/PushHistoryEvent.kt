/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.troubleshoot.impl.history

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId

sealed interface PushHistoryEvent {
    data class SetShowOnlyErrors(val showOnlyErrors: Boolean) : PushHistoryEvent
    data class Reset(val requiresConfirmation: Boolean) : PushHistoryEvent
    data class NavigateTo(val sessionId: SessionId, val roomId: RoomId, val eventId: EventId) : PushHistoryEvent
    data object ClearDialog : PushHistoryEvent
}
