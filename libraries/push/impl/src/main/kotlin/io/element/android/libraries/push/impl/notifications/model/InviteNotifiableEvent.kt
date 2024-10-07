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

data class InviteNotifiableEvent(
    override val sessionId: SessionId,
    override val roomId: RoomId,
    override val eventId: EventId,
    override val editedEventId: EventId?,
    override val canBeReplaced: Boolean,
    val roomName: String?,
    val noisy: Boolean,
    val title: String?,
    override val description: String,
    val type: String?,
    val timestamp: Long,
    val soundName: String?,
    override val isRedacted: Boolean = false,
    override val isUpdated: Boolean = false
) : NotifiableEvent
