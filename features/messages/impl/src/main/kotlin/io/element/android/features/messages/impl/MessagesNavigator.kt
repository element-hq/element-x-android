/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl

import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import kotlinx.collections.immutable.ImmutableList

interface MessagesNavigator {
    fun onShowEventDebugInfoClick(eventId: EventId?, debugInfo: TimelineItemDebugInfo)
    fun onForwardEventClick(eventId: EventId)
    fun onReportContentClick(eventId: EventId, senderId: UserId)
    fun onEditPollClick(eventId: EventId)
    fun onPreviewAttachment(attachments: ImmutableList<Attachment>)
    fun onNavigateToRoom(roomId: RoomId)
}
