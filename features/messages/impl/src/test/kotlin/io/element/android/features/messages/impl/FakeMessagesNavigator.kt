/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl

import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import io.element.android.tests.testutils.lambda.lambdaError
import kotlinx.collections.immutable.ImmutableList

class FakeMessagesNavigator(
    private val onShowEventDebugInfoClickLambda: (eventId: EventId?, debugInfo: TimelineItemDebugInfo) -> Unit = { _, _ -> lambdaError() },
    private val onForwardEventClickLambda: (eventId: EventId) -> Unit = { _ -> lambdaError() },
    private val onReportContentClickLambda: (eventId: EventId, senderId: UserId) -> Unit = { _, _ -> lambdaError() },
    private val onEditPollClickLambda: (eventId: EventId) -> Unit = { _ -> lambdaError() },
    private val onPreviewAttachmentLambda: (attachments: ImmutableList<Attachment>, inReplyToEventId: EventId?) -> Unit = { _, _ -> lambdaError() },
    private val onNavigateToRoomLambda: (roomId: RoomId, threadId: EventId?, serverNames: List<String>) -> Unit = { _, _, _ -> lambdaError() },
    private val onOpenThreadLambda: (threadRootId: ThreadId, focusedEventId: EventId?) -> Unit = { _, _ -> lambdaError() },
    private val closeLambda: () -> Unit = { lambdaError() },
) : MessagesNavigator {
    override fun navigateToEventDebugInfo(eventId: EventId?, debugInfo: TimelineItemDebugInfo) {
        onShowEventDebugInfoClickLambda(eventId, debugInfo)
    }

    override fun forwardEvent(eventId: EventId) {
        onForwardEventClickLambda(eventId)
    }

    override fun navigateToReportMessage(eventId: EventId, senderId: UserId) {
        onReportContentClickLambda(eventId, senderId)
    }

    override fun navigateToEditPoll(eventId: EventId) {
        onEditPollClickLambda(eventId)
    }

    override fun navigateToPreviewAttachments(attachments: ImmutableList<Attachment>, inReplyToEventId: EventId?) {
        onPreviewAttachmentLambda(attachments, inReplyToEventId)
    }

    override fun navigateToRoom(roomId: RoomId, eventId: EventId?, serverNames: List<String>) {
        onNavigateToRoomLambda(roomId, eventId, serverNames)
    }

    override fun navigateToThread(threadRootId: ThreadId, focusedEventId: EventId?) {
        onOpenThreadLambda(threadRootId, focusedEventId)
    }

    override fun close() {
        closeLambda()
    }
}
