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

package io.element.android.features.messages.impl.timeline.model.event

import androidx.compose.runtime.Immutable
import org.jsoup.nodes.Document

/**
 * Represents a text based content of a timeline item event (a message, a notice, an emote event...).
 */
@Immutable
sealed interface TimelineItemTextBasedContent : TimelineItemEventContent {
    /** The raw body of the event, in Markdown format. */
    val body: String

    /** The body of the event, with mentions replaced by their pillified version. */
    val pillifiedBody: CharSequence

    /** The parsed HTML DOM of the formatted event body. */
    val htmlDocument: Document?

    /** The formatted body of the event, already parsed and with the DOM translated to Android spans. */
    val formattedBody: CharSequence?

    /** The plain text version of the event body. This is the Markdown version without actual Markdown formatting. */
    val plainText: String

    /** Whether the event has been edited. */
    val isEdited: Boolean

    /** The raw HTML body of the event. */
    val htmlBody: String?
        get() = htmlDocument?.body()?.html()
}
