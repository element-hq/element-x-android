/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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

    data class Attachment(
        val allowCaption: Boolean,
        val showCaptionCompatibilityWarning: Boolean,
    ) : MessageComposerMode

    sealed interface Special : MessageComposerMode

    data class Edit(
        val eventOrTransactionId: EventOrTransactionId,
        val content: String
    ) : Special

    data class EditCaption(
        val eventOrTransactionId: EventOrTransactionId,
        val content: String,
        val showCaptionCompatibilityWarning: Boolean,
    ) : Special

    data class Reply(
        val replyToDetails: InReplyToDetails,
        val hideImage: Boolean,
    ) : Special {
        val eventId: EventId = replyToDetails.eventId()
    }

    val isEditing: Boolean
        get() = this is Edit || this is EditCaption

    val isReply: Boolean
        get() = this is Reply

    val inThread: Boolean
        get() = this is Reply &&
            replyToDetails is InReplyToDetails.Ready &&
            replyToDetails.eventContent is MessageContent &&
            (replyToDetails.eventContent as MessageContent).isThreaded
}

fun MessageComposerMode.showCaptionCompatibilityWarning(): Boolean {
    return when (this) {
        is MessageComposerMode.Attachment -> showCaptionCompatibilityWarning
        is MessageComposerMode.EditCaption -> showCaptionCompatibilityWarning && content.isEmpty()
        else -> false
    }
}
