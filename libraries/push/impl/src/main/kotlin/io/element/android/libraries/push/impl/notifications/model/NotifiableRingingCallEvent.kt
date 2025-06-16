/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.model

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.notification.CallNotifyType

data class NotifiableRingingCallEvent(
    override val sessionId: SessionId,
    override val roomId: RoomId,
    override val eventId: EventId,
    override val editedEventId: EventId?,
    override val description: String?,
    override val canBeReplaced: Boolean,
    override val isRedacted: Boolean,
    override val isUpdated: Boolean,
    val roomName: String?,
    val senderId: UserId,
    val senderDisambiguatedDisplayName: String?,
    val senderAvatarUrl: String?,
    val roomAvatarUrl: String? = null,
    val callNotifyType: CallNotifyType,
    val timestamp: Long,
) : NotifiableEvent
