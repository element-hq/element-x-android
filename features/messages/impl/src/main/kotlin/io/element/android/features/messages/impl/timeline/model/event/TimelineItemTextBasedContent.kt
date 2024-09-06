/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
