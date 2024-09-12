/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.model

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId

/**
 * Parent interface for all events which can be displayed as a Notification.
 */
sealed interface NotifiableEvent {
    val sessionId: SessionId
    val roomId: RoomId
    val eventId: EventId
    val editedEventId: EventId?
    val description: String?

    // Used to know if event should be replaced with the one coming from eventstream
    val canBeReplaced: Boolean
    val isRedacted: Boolean
    val isUpdated: Boolean
}
