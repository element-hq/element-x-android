/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.model

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId

sealed interface ResolvedPushEvent {
    val sessionId: SessionId
    val roomId: RoomId
    val eventId: EventId

    data class Event(val notifiableEvent: NotifiableEvent) : ResolvedPushEvent {
        override val sessionId: SessionId = notifiableEvent.sessionId
        override val roomId: RoomId = notifiableEvent.roomId
        override val eventId: EventId = notifiableEvent.eventId
    }

    data class Redaction(
        override val sessionId: SessionId,
        override val roomId: RoomId,
        val redactedEventId: EventId,
        val reason: String?,
    ) : ResolvedPushEvent {
        override val eventId: EventId = redactedEventId
    }
}
