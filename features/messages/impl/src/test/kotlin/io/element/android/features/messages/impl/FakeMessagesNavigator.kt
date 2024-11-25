/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl

import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import io.element.android.tests.testutils.lambda.lambdaError
import kotlinx.collections.immutable.ImmutableList

class FakeMessagesNavigator(
    private val onShowEventDebugInfoClickLambda: (eventId: EventId?, debugInfo: TimelineItemDebugInfo) -> Unit = { _, _ -> lambdaError() },
    private val onForwardEventClickLambda: (eventId: EventId) -> Unit = { _ -> lambdaError() },
    private val onReportContentClickLambda: (eventId: EventId, senderId: UserId) -> Unit = { _, _ -> lambdaError() },
    private val onEditPollClickLambda: (eventId: EventId) -> Unit = { _ -> lambdaError() },
    private val onPreviewAttachmentLambda: (attachments: ImmutableList<Attachment>) -> Unit = { _ -> lambdaError() },
) : MessagesNavigator {
    override fun onShowEventDebugInfoClick(eventId: EventId?, debugInfo: TimelineItemDebugInfo) {
        onShowEventDebugInfoClickLambda(eventId, debugInfo)
    }

    override fun onForwardEventClick(eventId: EventId) {
        onForwardEventClickLambda(eventId)
    }

    override fun onReportContentClick(eventId: EventId, senderId: UserId) {
        onReportContentClickLambda(eventId, senderId)
    }

    override fun onEditPollClick(eventId: EventId) {
        onEditPollClickLambda(eventId)
    }

    override fun onPreviewAttachment(attachments: ImmutableList<Attachment>) {
        onPreviewAttachmentLambda(attachments)
    }
}
