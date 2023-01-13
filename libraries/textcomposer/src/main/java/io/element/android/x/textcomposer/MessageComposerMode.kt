/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.x.textcomposer

import android.os.Parcelable
import io.element.android.x.matrix.core.EventId
import kotlinx.parcelize.Parcelize

sealed interface MessageComposerMode : Parcelable {
    @Parcelize
    data class Normal(val content: CharSequence?) : MessageComposerMode

    sealed class Special(open val eventId: EventId, open val defaultContent: CharSequence) :
        MessageComposerMode

    @Parcelize
    data class Edit(override val eventId: EventId, override val defaultContent: CharSequence) :
        Special(eventId, defaultContent)

    @Parcelize
    class Quote(override val eventId: EventId, override val defaultContent: CharSequence) :
        Special(eventId, defaultContent)

    @Parcelize
    class Reply(
        val senderName: String,
        override val eventId: EventId,
        override val defaultContent: CharSequence
    ) : Special(eventId, defaultContent)

    val relatedEventId: EventId?
        get() = when (this) {
            is Normal -> null
            is Edit -> eventId
            is Quote -> eventId
            is Reply -> eventId
        }
}
