/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.model

import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToDetails
import io.element.android.libraries.matrix.ui.messages.reply.eventId

@Immutable
sealed interface MessageComposerMode {
    data object Normal : MessageComposerMode

    sealed interface Special : MessageComposerMode

    data class Edit(
        val eventOrTransactionId: EventOrTransactionId,
        val content: String
    ) : Special

    data class Reply(
        val replyToDetails: InReplyToDetails,
        val hideImage: Boolean,
    ) : Special {
        val eventId: EventId = replyToDetails.eventId()
    }

    val relatedEventId: EventId?
        get() = when (this) {
            is Normal -> null
            is Edit -> eventOrTransactionId.eventId
            is Reply -> eventId
        }

    val isEditing: Boolean
        get() = this is Edit

    val isReply: Boolean
        get() = this is Reply

    val inThread: Boolean
        get() = this is Reply &&
            replyToDetails is InReplyToDetails.Ready &&
            replyToDetails.eventContent is MessageContent &&
            (replyToDetails.eventContent as MessageContent).isThreaded
}
