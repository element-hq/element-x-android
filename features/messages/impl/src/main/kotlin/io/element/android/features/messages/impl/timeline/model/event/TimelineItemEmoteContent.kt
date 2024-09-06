/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.event

import io.element.android.libraries.matrix.ui.messages.toPlainText
import org.jsoup.nodes.Document

data class TimelineItemEmoteContent(
    override val body: String,
    override val pillifiedBody: CharSequence = body,
    override val htmlDocument: Document?,
    override val plainText: String = htmlDocument?.toPlainText() ?: body,
    override val formattedBody: CharSequence?,
    override val isEdited: Boolean,
) : TimelineItemTextBasedContent {
    override val type: String = "TimelineItemEmoteContent"
}
