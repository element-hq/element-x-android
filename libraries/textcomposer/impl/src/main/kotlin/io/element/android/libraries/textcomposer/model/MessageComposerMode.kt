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

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnailInfo
import kotlinx.parcelize.Parcelize

@Immutable
sealed interface MessageComposerMode : Parcelable {
    @Parcelize
    data object Normal : MessageComposerMode

    sealed class Special(open val eventId: EventId?, open val defaultContent: String) :
        MessageComposerMode

    @Parcelize
    data class Edit(override val eventId: EventId?, override val defaultContent: String, val transactionId: TransactionId?) :
        Special(eventId, defaultContent)

    @Parcelize
    class Quote(override val eventId: EventId, override val defaultContent: String) :
        Special(eventId, defaultContent)

    @Parcelize
    class Reply(
        val senderName: String,
        val attachmentThumbnailInfo: AttachmentThumbnailInfo?,
        val isThreaded: Boolean,
        override val eventId: EventId,
        override val defaultContent: String
    ) : Special(eventId, defaultContent)

    val relatedEventId: EventId?
        get() = when (this) {
            is Normal -> null
            is Edit -> eventId
            is Quote -> eventId
            is Reply -> eventId
        }

    val isEditing: Boolean
        get() = this is Edit

    val isReply: Boolean
        get() = this is Reply

    val inThread: Boolean
        get() = this is Reply && isThreaded
}
