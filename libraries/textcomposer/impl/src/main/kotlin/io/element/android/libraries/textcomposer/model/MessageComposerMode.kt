/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.textcomposer.model

import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToDetails
import io.element.android.libraries.matrix.ui.messages.reply.eventId

@Immutable
sealed interface MessageComposerMode {
    data object Normal : MessageComposerMode

    sealed interface Special : MessageComposerMode

    data class Edit(
        val eventId: EventId?,
        val transactionId: TransactionId?,
        val content: String
    ) : Special

    data class Reply(
        val replyToDetails: InReplyToDetails
    ) : Special {
        val eventId: EventId = replyToDetails.eventId()
    }

    val relatedEventId: EventId?
        get() = when (this) {
            is Normal -> null
            is Edit -> eventId
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
