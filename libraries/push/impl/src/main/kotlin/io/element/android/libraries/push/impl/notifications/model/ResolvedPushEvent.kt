/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.model

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId

sealed interface ResolvedPushEvent {
    data class Event(val notifiableEvent: NotifiableEvent) : ResolvedPushEvent

    data class Redaction(
        val sessionId: SessionId,
        val roomId: RoomId,
        val redactedEventId: EventId,
        val reason: String?,
    ) : ResolvedPushEvent
}
